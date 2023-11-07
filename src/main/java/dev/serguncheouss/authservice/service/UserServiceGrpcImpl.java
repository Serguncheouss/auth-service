package dev.serguncheouss.authservice.service;

import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import dev.serguncheouss.authservice.grpc.*;
import dev.serguncheouss.authservice.model.Role;
import dev.serguncheouss.authservice.model.User;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import jakarta.security.auth.message.AuthException;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;


@GrpcService
public class UserServiceGrpcImpl extends UserServiceGrpc.UserServiceImplBase {
    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private TokenService tokenService;

    @Override
    public void create(
            CreateUserRequest request, StreamObserver<GRPCUser> responseObserver) {
        try {
            final User user = userService.create(request.getUsername(), request.getPassword(), request.getIsActive());

            GRPCUser response = GRPCUserFromUser(user);

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (DataIntegrityViolationException e) {
            Status status = Status.ALREADY_EXISTS.withDescription("User is already exists.");
            responseObserver.onError(status.asRuntimeException());
        }
    }

    @Override
    public void get(GetUserRequest request, StreamObserver<GRPCUser> responseObserver) {
        final User user = userService.getById(UUID.fromString(request.getId()));

        if (user == null) {
            Status status = Status.NOT_FOUND.withDescription("User is not found.");
            responseObserver.onError(status.asRuntimeException());
            return;
        }

        GRPCUser response = GRPCUserFromUser(user);

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getAll(Empty request, StreamObserver<UserListResponse> responseObserver) {
        final List<User> users = userService.getAll();

        UserListResponse response = UserListResponse.newBuilder()
                .addAllUsers(
                        users.stream()
                                .map(UserServiceGrpcImpl::GRPCUserFromUser)
                                .toList()
                ).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void update(GRPCUser request, StreamObserver<Empty> responseObserver) {
        final User user = userFromGRPCUser(request);
        if (userService.update(user)) {
            responseObserver.onNext(Empty.newBuilder().build());
            responseObserver.onCompleted();
        } {
            Status status = Status.NOT_FOUND.withDescription("User is not found.");
            responseObserver.onError(status.asRuntimeException());
        }
    }

    @Override
    public void checkToken(CheckTokenRequest request, StreamObserver<CheckTokenResponse> responseObserver) {
        try {
            final String username = tokenService.getUsernameFromAccessToken(request.getAccessToken());
            final User user = userService.getByUsername(username);

            if (user == null) {
                throw new AuthException("User is not found.");
            } else if (!user.getIsActive()) {
                throw new AuthException("User is not active.");
            }

            CheckTokenResponse response = CheckTokenResponse.newBuilder()
                    .setId(user.getId().toString())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (AuthException e) {
            Status status = Status.UNAUTHENTICATED.withDescription(e.getMessage());
            responseObserver.onError(status.asRuntimeException());
        }
    }

    @Override
    public void delete(GetUserRequest request, StreamObserver<Empty> responseObserver) {
        if (userService.delete(UUID.fromString(request.getId()))) {
            responseObserver.onNext(Empty.newBuilder().build());
            responseObserver.onCompleted();
        } else {
            Status status = Status.NOT_FOUND.withDescription("User is not found.");
            responseObserver.onError(status.asRuntimeException());
        }
    }

    private static GRPCUser GRPCUserFromUser(User user) {
        return GRPCUser.newBuilder()
                .setId(user.getId().toString())
                .setUsername(user.getUsername())
                .setPassword(user.getPassword())
                .setAccessToken(user.getAccessToken())
                .setRefreshToken(user.getRefreshToken())
                .setIsActive(user.getIsActive().toString())
                .setCreateDate(Timestamp.newBuilder()
                        .setSeconds(user.getCreateDate().getEpochSecond())
                        .setNanos(user.getCreateDate().getNano())
                        .build())
                .addAllRoles(user.getRoles().stream().map(Role::getName).toList())
                .build();
    }

    private User userFromGRPCUser(GRPCUser grpcUser) {
        final User user = new User(grpcUser.getUsername(), grpcUser.getPassword());
        user.setId(UUID.fromString(grpcUser.getId()));
        user.setAccessToken(grpcUser.getAccessToken());
        user.setRefreshToken(grpcUser.getRefreshToken());
        user.setIsActive(Boolean.valueOf(grpcUser.getIsActive()));
        user.setRoles(grpcUser.getRolesList().stream()
                .collect(
                        HashSet::new,
                        (s, n) -> roleService.getByName(n).ifPresent(s::add),
                        Set::addAll
                )
        );
        return user;
    }
}
