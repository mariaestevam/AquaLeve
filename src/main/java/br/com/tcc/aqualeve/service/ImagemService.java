package br.com.tcc.aqualeve.service;

import java.io.*;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import br.com.tcc.aqualeve.exception.RegraNegocioException;

@Service
public class ImagemService {
    public record Foto(byte[] dados, String tipo) {}
    public Foto validar(MultipartFile arquivo) {
        if (arquivo.getSize() > 5 * 1024 * 1024) throw new RegraNegocioException("A foto deve ter no máximo 5 MB.");
        try (ImageInputStream entrada = ImageIO.createImageInputStream(arquivo.getInputStream())) {
            var leitores = ImageIO.getImageReaders(entrada);
            if (!leitores.hasNext()) throw new RegraNegocioException("Envie uma imagem JPEG ou PNG válida.");
            ImageReader leitor = leitores.next();
            try {
                leitor.setInput(entrada);
                String formato = leitor.getFormatName().toLowerCase(java.util.Locale.ROOT);
                if (!java.util.Set.of("jpeg", "jpg", "png").contains(formato))
                    throw new RegraNegocioException("Envie uma imagem JPEG ou PNG válida.");
                if ((long) leitor.getWidth(0) * leitor.getHeight(0) > 16000000)
                    throw new RegraNegocioException("A foto deve ter no máximo 16 milhões de pixels.");
                // Decodifica e regrava: não confia na extensão nem no Content-Type enviado.
                var imagem = leitor.read(0);
                ByteArrayOutputStream saida = new ByteArrayOutputStream();
                ImageIO.write(imagem, formato, saida);
                if (saida.size() > 5 * 1024 * 1024) throw new RegraNegocioException("A foto processada excede 5 MB. Reduza a imagem.");
                return new Foto(saida.toByteArray(), formato.equals("png") ? "image/png" : "image/jpeg");
            } finally { leitor.dispose(); }
        } catch (IOException | IllegalArgumentException e) {
            throw new RegraNegocioException("Não foi possível ler a imagem. Envie um JPEG ou PNG válido.");
        }
    }
}
