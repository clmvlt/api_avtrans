package bzh.stack.apiavtrans.config;

import bzh.stack.apiavtrans.interceptor.UserEmailInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final UserEmailInterceptor userEmailInterceptor;

    @Value("${app.upload.pictures-dir:uploads/pictures}")
    private String picturesDir;

    @Value("${app.upload.vehicules-dir:uploads/vehicules}")
    private String vehiculesDir;

    @Value("${app.upload.vehicules-profile-dir:uploads/vehicules/profile}")
    private String vehiculesProfileDir;

    @Value("${app.upload.vehicules-adjust-dir:uploads/vehicules/adjust}")
    private String vehiculesAdjustDir;

    @Value("${app.upload.vehicule-files-dir:uploads/vehicule-files}")
    private String vehiculeFilesDir;

    @Value("${app.upload.rapports-dir:uploads/rapports}")
    private String rapportsDir;

    @Value("${app.upload.apk-dir:uploads/apk}")
    private String apkDir;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userEmailInterceptor);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/pictures/**")
                .addResourceLocations("file:" + picturesDir + "/");

        registry.addResourceHandler("/uploads/vehicules/profile/**")
                .addResourceLocations("file:" + vehiculesProfileDir + "/");

        registry.addResourceHandler("/uploads/vehicules/adjust/**")
                .addResourceLocations("file:" + vehiculesAdjustDir + "/");

        registry.addResourceHandler("/uploads/vehicule-files/**")
                .addResourceLocations("file:" + vehiculeFilesDir + "/");

        registry.addResourceHandler("/uploads/vehicules/**")
                .addResourceLocations("file:" + vehiculesDir + "/");

        registry.addResourceHandler("/uploads/rapports/**")
                .addResourceLocations("file:" + rapportsDir + "/");

        registry.addResourceHandler("/uploads/apk/**")
                .addResourceLocations("file:" + apkDir + "/");
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        List<MediaType> mediaTypes = new ArrayList<>();
        mediaTypes.add(MediaType.APPLICATION_JSON);
        mediaTypes.add(MediaType.TEXT_PLAIN);
        jsonConverter.setSupportedMediaTypes(mediaTypes);
        converters.add(jsonConverter);
    }
}