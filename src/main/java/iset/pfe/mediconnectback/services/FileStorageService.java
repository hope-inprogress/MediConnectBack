package iset.pfe.mediconnectback.services;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {
    
    private final String uploadDir = "uploads/medical_files/";

    private  final Path fileStorageLocation;

    public FileStorageService() throws IOException {
        this.fileStorageLocation = Paths.get("uploads/medical_files").toAbsolutePath().normalize();
        Files.createDirectories(Paths.get(uploadDir + "temp/")); 
    }

    public String storeFile(MultipartFile file) {

        if (file.isEmpty()) {
            throw new RuntimeException("Cannot store empty file ");
        }

        String originalFileName = file.getOriginalFilename();
        String cleanedFileName = UUID.randomUUID() + "_" + (originalFileName != null ? originalFileName.replaceAll("\s+", "_") : "file");
        
        try {
            Path targetLocation = this.fileStorageLocation.resolve(cleanedFileName);
            Files.copy(file.getInputStream(), targetLocation);
            return "/uploads/medical_files/" + cleanedFileName; // Return the relative path to the file
        } catch (IOException e) {
            throw new RuntimeException("Could not store file " + cleanedFileName + ". Please try again!", e);
        }
    }

    public Resource loadFileAsResource(String fichier) {
        try {
            Path filePath = fileStorageLocation.resolve(fichier).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found " + fichier);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("File not found " + fichier, e);
        }

    }
}
