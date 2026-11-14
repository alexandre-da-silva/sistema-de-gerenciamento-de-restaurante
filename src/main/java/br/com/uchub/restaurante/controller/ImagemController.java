package br.com.uchub.restaurante.controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.core.io.*;
import org.springframework.web.server.ResponseStatusException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;

@RestController
public class ImagemController {
 private final Path diretorio;
 public ImagemController(@Value("${app.upload-dir:./uploads/produtos}") String dir) {diretorio=Path.of(dir).toAbsolutePath().normalize();}
 @PostMapping(value="/api/v1/admin/imagens",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
 public Map<String,String> upload(@RequestParam("arquivo") MultipartFile arquivo) throws Exception {
  if(arquivo.isEmpty()||arquivo.getSize()>5*1024*1024)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Envie uma foto JPG ou PNG de até 5 MB.");
  BufferedImage imagem;
  try(var input=ImageIO.createImageInputStream(arquivo.getInputStream())) {
   var readers=ImageIO.getImageReaders(input);
   if(!readers.hasNext())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Imagem inválida.");
   var reader=readers.next();
   try {reader.setInput(input);String format=reader.getFormatName();
    if(!(format.equalsIgnoreCase("JPEG")||format.equalsIgnoreCase("PNG"))||reader.getWidth(0)>6000||reader.getHeight(0)>6000||((long)reader.getWidth(0))*reader.getHeight(0)>20000000)
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Use JPG/PNG com até 6000 px e 20 megapixels.");
    imagem=reader.read(0);
   } finally {reader.dispose();}
  }
  int width=Math.min(1600,imagem.getWidth()),height=Math.max(1,imagem.getHeight()*width/imagem.getWidth());
  var safe=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);var g=safe.createGraphics();
  g.setColor(java.awt.Color.WHITE);g.fillRect(0,0,width,height);g.drawImage(imagem,0,0,width,height,null);g.dispose();
  Files.createDirectories(diretorio);String nome=UUID.randomUUID()+".jpg";
  ImageIO.write(safe,"jpg",diretorio.resolve(nome).toFile());
  return Map.of("url","/media/"+nome);
 }
 @GetMapping("/media/{nome:[a-f0-9-]+\\.jpg}")
 public ResponseEntity<Resource> imagem(@PathVariable String nome) {
  Path file=diretorio.resolve(nome).normalize();
  if(!file.startsWith(diretorio)||!Files.isRegularFile(file))throw new ResponseStatusException(HttpStatus.NOT_FOUND);
  return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).cacheControl(CacheControl.maxAge(java.time.Duration.ofDays(7))).body(new FileSystemResource(file));
 }
}
