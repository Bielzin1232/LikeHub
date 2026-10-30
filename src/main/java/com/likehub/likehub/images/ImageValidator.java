package com.likehub.likehub.images;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import com.likehub.likehub.shared.exceptions.RequisicaoInvalidaException;
import org.springframework.web.multipart.MultipartFile;

public final class ImageValidator {

    private static final Set<String> ALLOWED = Set.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_BYTES = 5L * 1024 * 1024;

    private ImageValidator() {}

    public static void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RequisicaoInvalidaException("ARQUIVO_VAZIO", "Arquivo vazio");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new RequisicaoInvalidaException("ARQUIVO_MAIOR_QUE_5MB", "Arquivo maior que 5MB");
        }
        try (InputStream in = file.getInputStream()) {
            BufferedImage img = ImageIO.read(in);
            if (img == null) {
                throw new RequisicaoInvalidaException("IMAGEM_INVALIDA", "Arquivo não é imagem válida");
            }
        } catch (IOException e) {
            throw new RequisicaoInvalidaException("ARQUIVO_CORROMPIDO", "Arquivo corrompido");
        }
        if (file.getContentType() == null || !ALLOWED.contains(file.getContentType())) {
            throw new RequisicaoInvalidaException("FORMATO_NAO_SUPORTADO", "Formato não suportado");
        }
    }
}