package com.nexus.service;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@Service
public class StorageService {
    private final Cloudinary cloudinary;
    public StorageService(@Value("${cloudinary.cloud_name}") String cn,
                          @Value("${cloudinary.api_key}")    String ak,
                          @Value("${cloudinary.api_secret}") String as) {
        this.cloudinary=new Cloudinary(ObjectUtils.asMap("cloud_name",cn,"api_key",ak,"api_secret",as,"secure",true));
    }
    public String subirImagen(MultipartFile f) {
        if(f==null||f.isEmpty())return null;
        try{ @SuppressWarnings("unchecked") Map<String,Object> r=(Map<String,Object>)cloudinary.uploader()
            .upload(f.getBytes(),ObjectUtils.asMap("folder","nexus","resource_type","image","quality","auto:good","fetch_format","auto"));
            return(String)r.get("secure_url"); }catch(Exception e){System.err.println("subir imagen: "+e.getMessage());return null;}
    }
    public String subirAudio(MultipartFile f) {
        if(f==null||f.isEmpty())return null;
        try{ @SuppressWarnings("unchecked") Map<String,Object> r=(Map<String,Object>)cloudinary.uploader()
            .upload(f.getBytes(),ObjectUtils.asMap("folder","nexus/audio","resource_type","video"));
            return(String)r.get("secure_url"); }catch(Exception e){System.err.println("subir audio: "+e.getMessage());return null;}
    }
    /** Requerido por ChatService line 43 */
    public String subirVideo(MultipartFile f) {
        if(f==null||f.isEmpty())return null;
        try{ @SuppressWarnings("unchecked") Map<String,Object> r=(Map<String,Object>)cloudinary.uploader()
            .upload(f.getBytes(),ObjectUtils.asMap("folder","nexus/video","resource_type","video"));
            return(String)r.get("secure_url"); }catch(Exception e){System.err.println("subir video: "+e.getMessage());return null;}
    }
    public List<String> subirImagenes(List<MultipartFile> archivos) {
        List<String> urls=new ArrayList<>(); if(archivos==null)return urls;
        for(MultipartFile f:archivos){String u=subirImagen(f);if(u!=null)urls.add(u);} return urls;
    }
    /**
     * Requerido en OfertaController, ProductoController, UsuarioController, VehiculoController.
     * Retorna void para que forEach(storageService::eliminarImagen) compile con List<String>.
     */
    public void eliminarImagen(String url) {
        if(url==null||url.isBlank())return;
        String id=extraerPublicId(url); if(id!=null)eliminar(id);
    }
    public boolean eliminar(String publicId) {
        if(publicId==null||publicId.isBlank())return false;
        try{ @SuppressWarnings("unchecked") Map<String,Object> r=(Map<String,Object>)cloudinary.uploader()
            .destroy(publicId,ObjectUtils.emptyMap()); return"ok".equals(r.get("result")); }
        catch(Exception e){System.err.println("eliminar: "+e.getMessage());return false;}
    }
    public String extraerPublicId(String url) {
        if(url==null||!url.contains("cloudinary"))return null;
        try{ String[]p=url.split("/upload/"); if(p.length<2)return null;
            String s=p[1].replaceAll("^v\\d+/",""); int d=s.lastIndexOf('.');
            return d>0?s.substring(0,d):s; }catch(Exception e){return null;}
    }
}
