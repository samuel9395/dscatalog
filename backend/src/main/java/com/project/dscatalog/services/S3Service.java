package com.project.dscatalog.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

/**
 * Camada de serviço para operações de upload de arquivos para o Amazon S3.
 */
@Service
public class S3Service {

    private static Logger LOG = LoggerFactory.getLogger(S3Service.class);

    @Autowired
    private AmazonS3 s3client;

    @Value("${s3.bucket}")
    private String bucketName;

    /**
     * Realiza o upload de um arquivo para o bucket do Amazon S3.
     * <p>
     * Este método gera um identificador único (UUID) para o arquivo a fim de evitar
     * sobrescritas no storage, preservando a extensão original.
     * </p>
     *
     * @param file Arquivo recebido na requisição HTTP (Multipart).
     * @return {@link URL} pública de acesso ao arquivo armazenado no S3.
     * @throws IllegalArgumentException Se ocorrer uma falha ao ler o fluxo de dados do arquivo.
     */
    public URL uploadFile(MultipartFile file) {
        try {
            // Extrai a extensão do arquivo original e gera um nome único com UUID
            String originalName = file.getOriginalFilename();
            String extension = FilenameUtils.getExtension(originalName);
            String fileName = UUID.randomUUID().toString() + "." + extension;

            // Extrai o fluxo de dados e o tipo de conteúdo para envio
            InputStream is = file.getInputStream();
            String contentType = file.getContentType();

            return uploadFile(is, fileName, contentType);
        } catch (IOException e) {
            throw new IllegalArgumentException("Erro ao processar o arquivo para upload: " + e.getMessage(), e);
        }
    }

    /**
     * Envia o fluxo de dados (InputStream) de um arquivo para o Amazon S3 e gera sua URL.
     *
     * @param is          Fluxo de entrada dos dados do arquivo.
     * @param fileName    Nome do arquivo que será salvo no bucket S3.
     * @param contentType Tipo do conteúdo (MIME type), como 'image/png' ou 'application/pdf'.
     * @return {@link URL} de acesso direto ao objeto gravado no bucket.
     * @throws com.amazonaws.SdkClientException Se ocorrer uma falha de comunicação ou autenticação com a AWS.
     */
    private URL uploadFile(InputStream is, String fileName, String contentType) {

        // Configura os metadados do arquivo (essencial para que o navegador exiba o arquivo corretamente)
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);

        LOG.info("Iniciando upload do arquivo: {}", fileName);
        s3client.putObject(bucketName, fileName, is, metadata);
        LOG.info("Upload do arquivo {} concluído com sucesso", fileName);

        return s3client.getUrl(bucketName, fileName);
    }
}
