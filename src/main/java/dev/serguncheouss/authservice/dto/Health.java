package dev.serguncheouss.authservice.dto;

import lombok.*;

@Data
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class Health {
    @AllArgsConstructor
    public enum Status {
        UP("UP"),
        DOWN("DOWN");

        @Getter
        private final String status;

        @Override
        public String toString() {
            return status;
        }
    }


    private Status status;
}
