package com.likehub.likehub.shared.exceptions;

import java.time.Instant;

public record ErroResponse(Instant timestamp, int status, String mensagem) {
}
