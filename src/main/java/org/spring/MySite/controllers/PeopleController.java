package org.spring.MySite.controllers;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.spring.MySite.models.Person;
import org.spring.MySite.repositories.PeopleRepository;
import org.spring.MySite.security.P;
import org.spring.MySite.security.PersonDetails;
import org.spring.MySite.services.PeopleService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class PeopleController {

    private PeopleService peopleService;
    private JavaMailSender mailSender;
    private SessionRegistry sessionRegistry;
    private OAuth2AuthorizedClientService authorizedClientService;

    @Value("${pathToDirectory:/home/karina/ProgJava/imagecab/}")
    //@Value("${pathToDirectory:/app/imagecab/}")
    String pathToDirectory;

    @Value("${spring.security.oauth2.client.registration.github.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.github.client-secret}")
    private String clientSecret;

    @Autowired
    public PeopleController(PeopleService peopleService, JavaMailSender mailSender, SessionRegistry sessionRegistry, OAuth2AuthorizedClientService authorizedClientService) {
        this.peopleService = peopleService;
        this.mailSender = mailSender;
        this.sessionRegistry = sessionRegistry;
        this.authorizedClientService=authorizedClientService;
    }
    @GetMapping("/session")
    public String checkSession(HttpSession session) {
        Enumeration<String> attributes = session.getAttributeNames();
        while (attributes.hasMoreElements()) {
            String attr = attributes.nextElement();
            System.out.println(attr + ": " + session.getAttribute(attr));
        }
        return "indexMyPhoto";
    }


    @GetMapping("/per")
    public String showPerson(Model model) {
        return "person";
    }

    @GetMapping("/param")
    public String showParam(Model model) {
        model.addAttribute("p", "200");
        return "parametr";
    }

    @GetMapping()
    public String showFirst(Authentication authentication, Model model) {
        //Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("authentication in / "+authentication);

        if ((!(authentication instanceof AnonymousAuthenticationToken)) && authentication != null) {

            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            for (GrantedAuthority grantedAuthority : authorities) {
                if (grantedAuthority.getAuthority().equals("BLOCKED")) {
                    return "blockPage";
                }
                if (grantedAuthority.getAuthority().equals("newOAuth2")) {
                    return "redirect:/oauth2/password";
                }
            }
                return "redirect:/home";
            }
            return "first";
        }



    @GetMapping("/home")
    public String showHome(Model model, @P Person personLogged) {
        System.out.println(personLogged);
        model.addAttribute("person", personLogged);
        return "index";
    }


    @GetMapping("/myPage")
    public String showMyPage(Model model, @P Person personLogged) {
        model.addAttribute("person", personLogged);
        return "indexMyPage";
    }

    /*@GetMapping("/news")
    public String news() {return "indexNews";}*/

    @GetMapping("/myPage/photo")
    public String photoNew(Model model, @P Person personLogged) {
        model.addAttribute("person", personLogged);
        return "photo";
    }

    @GetMapping("/expiredSession")
    public String expiredSession() {
        return "expiredSession";
    }

    @DeleteMapping()
    public String deletePerson(@P Person personLogged, Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        //System.out.println("Принципал " +principal);
        if (authentication instanceof OAuth2AuthenticationToken authToken) {

            try {
                // Отзываем токен GitHub
                revokeGithubToken(authToken);
                authorizedClientService.removeAuthorizedClient("github", authToken.getName());
            } catch (Exception e) {
                throw new AuthenticationServiceException("GitHub OAuth revocation failed", e);
            }
        }

        peopleService.deleteById(personLogged.getId());

        PersonDetails personDetails = new PersonDetails(personLogged);
        //System.out.println("Details" +personDetails);
        invalidateAllSessions(personDetails);

        SecurityContextHolder.clearContext();
        //SecurityContextHolder.getContext().setAuthentication(null);
        new SecurityContextLogoutHandler().logout(request, response, authentication);

        return "redirect:/";
    }

    private void revokeGithubToken(OAuth2AuthenticationToken authToken) {

        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient("github", authToken.getName()); //вместо "github" можно authToken.getAuthorizedClientRegistrationId();
        String accessToken = client.getAccessToken().getTokenValue();
        if (accessToken != null && clientId != null && clientSecret !=null) {
            System.out.println(accessToken);
            System.out.println(clientId);
            System.out.println(clientSecret );
            String revokeUrl = String.format("https://api.github.com/applications/%s/token", clientId);

            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(clientId, clientSecret);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String requestBody = "{\"access_token\":\"" + accessToken + "\"}";

            new RestTemplate().exchange(revokeUrl, HttpMethod.DELETE, new HttpEntity<>(requestBody, headers), Void.class);
        }
    }


    private void invalidateAllSessions(PersonDetails personDetails) {
        List<SessionInformation> sessions = sessionRegistry.getAllSessions(personDetails, false);
        for (SessionInformation session : sessions) {
            session.expireNow();
            sessionRegistry.removeSessionInformation(session.getSessionId());
        }
    }


    @PostMapping("/home")
    public String imageTheme(@ModelAttribute("person") Person person, @P Person updatedPerson) {
        updatedPerson.setImageTheme(person.getImageTheme());
        peopleService.save(updatedPerson);
        return "redirect:/home";
    }

    @PostMapping("/myPage")
    public String update(@Valid  @ModelAttribute("person")  Person person, BindingResult bindingResult, @P Person updatedPerson) {
        if(bindingResult.hasErrors()) {

            System.out.println(person);
            List<String> errors = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.toList());
            System.out.println(errors);
            return "indexMyPage"; }

        System.out.println(person);
        updatedPerson.setName(person.getName());
        updatedPerson.setSurname(person.getSurname());
        updatedPerson.setBirthdate(person.getBirthdate());
        peopleService.save(updatedPerson);
        return "redirect:/myPage";
    }

    @PostMapping("/myPage/photo/delete")
    public String deletePhoto(@ModelAttribute("person") Person person, @P Person updatedPerson) {
        updatedPerson.setPhoto(person.getPhoto());
        peopleService.save(updatedPerson);
        return "redirect:/myPage";
    }

    @PostMapping("/myPage/photo")
    public String photoCrop(@ModelAttribute("person") Person person, @P Person updatedPerson,
                            @RequestParam("x") String x, @RequestParam("y") String y,
                            @RequestParam("w") String w, @RequestParam("h") String h,
                            @RequestParam("widthImage") String widthImage) {

        //String pathToDirectory = "C:\\cab\\imagecab\\";
        //String pathToDirectory = "/app/imagecab/";

        System.out.println("person.getPhoto() " +person.getPhoto());
        if (!person.getPhoto().isEmpty()) {

            String[] stringsAfterSplit = person.getPhoto().split(",");

            if (stringsAfterSplit.length != 1) {

                String base64 = stringsAfterSplit[1];// Преобразование изображения через base64, удаление заголовка изображения (data: image / jpg; base64,)
                System.out.println("base64 " +base64);
                // 2, декодировать в байтовый массив

                byte[] data = Base64.getDecoder().decode(base64);
                // 3, файл байтового потока

                UUID uuid = UUID.randomUUID();
                String uuidAsString = uuid.toString();// uuid как имя файла при сохранении
                System.out.println("uuidAsString " +uuidAsString);

               /* File filepath = new File("C:\\progJava\\MySite\\MySite\\src\\main\\resources\\static\\images\\imagecab\\");//Создать папку
                if (!filepath.exists()) {// Если папки нет, создайте новую
                    filepath.mkdirs();
                } */

                File picfilepath = new File(pathToDirectory + uuidAsString + ".png"); //сохранить файл изображения в папку приложения

                //File picfilepathContainer = new File("C:\\progJava\\MySite\\MySite\\src\\main\\resources\\static\\images\\imagecab\\" + uuidAsString + ".png"); //сохранить файл изображения в


                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(picfilepath);
                    out.write(data);
                    out.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                cropPhoto(widthImage, x, y, w, h, uuidAsString + ".png");

                String filePathForHtml = "/imagecab/" + uuidAsString + ".png";
                System.out.println("filePathForHtml " +filePathForHtml);

                person.setPhoto(filePathForHtml);
                updatedPerson.setPhoto(person.getPhoto());
                peopleService.save(updatedPerson);

            } else {

                String[] stringsAfterSplit2 = person.getPhoto().split("/");
                System.out.println("stringsAfterSplit2 " +stringsAfterSplit2[2]);
                String str0 = stringsAfterSplit2[2];
                String[] stringsAfterSplit3 = str0.split("\\.");
                String str = stringsAfterSplit3[0];

                cropPhoto(widthImage, x, y, w, h, str + ".png");

            }

        }
        return "redirect:/myPage";

    }

    public void cropPhoto(String widthImage, String x, String y, String w, String h, String str ){

        //String pathToDirectory = "C:\\cab\\imagecab\\";
        //String pathToDirectory = "/app/imagecab/";

        int xx = Integer.parseInt(x);
        int yy = Integer.parseInt(y);
        int ww = Integer.parseInt(w);
        int hh = Integer.parseInt(h);

        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(pathToDirectory + str));
            //System.out.println(pathToDirectory + str);
        } catch (Exception ex) {
            System.err.println("read Image error "+ ex.getMessage());
        }
        if (image == null) {
            throw new IllegalArgumentException("Не удалось прочитать изображение. Возможно, неподдерживаемый формат.");
        }

        double rateX = (double)image.getWidth() / Integer.parseInt(widthImage);


        BufferedImage outB = image.getSubimage((int)(xx * rateX), (int)(yy * rateX), (int)(ww * rateX), (int)(hh * rateX));

        try {
            ImageIO.write(outB, "png", new File(pathToDirectory + str));


        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }

    }


/*

    /*@PostMapping("/myPage/photo")
    public String photoCrop(@ModelAttribute("person") Person person, @P Person updatedPerson,
                        @RequestParam("x") String x,  @RequestParam("y") String y,
                            @RequestParam("w") String w, @RequestParam("h") String h, @RequestParam("widthImage") String widthImage ) {


        if (!person.getPhoto().isEmpty()) {

            String[] stringsAfterSplit = person.getPhoto().split(",");

            if (stringsAfterSplit.length != 1) {

                String base64 = stringsAfterSplit[1];// Преобразование изображения через base64, удаление заголовка изображения (data: image / jpg; base64,)
                //System.out.println(base64);
                // 2, декодировать в байтовый массив

                byte[] data = Base64.getDecoder().decode(base64);
                //System.out.println(data);
                // 3, файл байтового потока

                UUID uuid = UUID.randomUUID();
                String uuidAsString = uuid.toString();// uuid как имя файла при сохранении
                //System.out.println(uuidAsString);*/


               /* File filepath = new File("C:\\progJava\\MySite\\MySite\\src\\main\\resources\\static\\images\\imagecab\\");//Создать папку
                if (!filepath.exists()) {// Если папки нет, создайте новую
                    filepath.mkdirs();
                } */

                /*File picfilepath = new File("C:\\progJava\\MySite\\MySite\\src\\main\\resources\\static\\images\\imagecab\\" + uuidAsString + ".png"); //сохранить файл изображения в папку приложения

                //File picfilepathContainer = new File("C:\\progJava\\MySite\\MySite\\src\\main\\resources\\static\\images\\imagecab\\" + uuidAsString + ".png"); //сохранить файл изображения в

                final String uploadLocation = getClass().getClassLoader().getResource("static").toString();
                //we should get rid of file:/, hence the substring
                Path uploadDirectory = Paths.get(uploadLocation.substring(6, uploadLocation.length()));
                System.out.println(uploadDirectory);
                File picfilepathTarget = new File(uploadDirectory + "\\images\\imagecab\\" + uuidAsString + ".png"); //сохранить файл изображения в папку target


                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(picfilepath);
                    out.write(data);
                    out.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    out = new FileOutputStream(picfilepathTarget);
                    out.write(data);
                    out.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                cropPhoto(widthImage, x, y, w, h, uuidAsString + ".png");

                String filePathForHtml = "/images/imagecab/" + uuidAsString + ".png";
                System.out.println(filePathForHtml);

                person.setPhoto(filePathForHtml);
                updatedPerson.setPhoto(person.getPhoto());
                peopleService.save(updatedPerson);

            } else {

                //String path = "D:images\\upload_final\\030311175258.jpg";

                String[] stringsAfterSplit2 = person.getPhoto().split("/");
                String str0 = stringsAfterSplit2[3];
                String[] stringsAfterSplit3 = str0.split("\\.");
                String str = stringsAfterSplit3[0];

                cropPhoto(widthImage, x, y, w, h, str + ".png");

            }

        }
        return "redirect:/myPage";
    }

    public void cropPhoto(String widthImage, String x, String y, String w, String h, String str ){

        int xx = Integer.parseInt(x);
        int yy = Integer.parseInt(y);
        int ww = Integer.parseInt(w);
        int hh = Integer.parseInt(h);

        BufferedImage image = null;
        try {
            image = ImageIO.read(new File("C:\\progJava\\MySite\\MySite\\src\\main\\resources\\static\\images\\imagecab\\" + str));
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }

        double rateX = (double)image.getWidth() / Integer.parseInt(widthImage);*/

        /*System.out.println(xx);
        System.out.println(yy);
        System.out.println(ww);
        System.out.println(hh);
        System.out.println(image.getWidth());
        System.out.println(Integer.parseInt(widthImage));
        System.out.println(rateX);*/

       /* BufferedImage outB = image.getSubimage((int)(xx * rateX), (int)(yy * rateX), (int)(ww * rateX), (int)(hh * rateX));

        final String uploadLocation = getClass().getClassLoader().getResource("static").toString();
        //we should get rid of file:/, hence the substring
        Path uploadDirectory = Paths.get(uploadLocation.substring(6, uploadLocation.length()));

        try {
            ImageIO.write(outB, "png", new File("C:\\progJava\\MySite\\MySite\\src\\main\\resources\\static\\images\\imagecab\\" + str));
            ImageIO.write(outB, "png", new File(uploadDirectory + "\\images\\imagecab\\" + str));

        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }

    }*/


    @PostMapping("/myPage/mail")
    public String eMail(@Valid @ModelAttribute("person") Person person, BindingResult bindingResult, @P Person personMail) {
        if(bindingResult.hasErrors()) {
            return "indexMyPage"; }

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
        String htmlMsg = person.getMessage();
//mimeMessage.setContent(htmlMsg, "text/html"); /** Use this or below line **/
        try {
            helper.setText(htmlMsg, true); // Use this or above line.
            helper.setTo("egorchik_mail@mail.ru"); //Site14789
            helper.setSubject("Сообщение от " + personMail.getUsername()+ " "+ personMail.getEmail());
            helper.setFrom("egorchik_mail@mail.ru");
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        mailSender.send(mimeMessage);

        return "redirect:/myPage";
    }

   //Вариант 1 -см. в PersonDetailService
   /* @ModelAttribute("person")
    public Person findOneByUsername() {
        User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<Person> person =  peopleRepository.findByUsername(user.getUsername());
        return person.orElse(null);
    }*/

    //Вариант 2 -см. в PersonDetailService
    //@ModelAttribute("person")
    /*public Person findOneByUsername() {
        PersonDetails personDetails = (PersonDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Person person = personDetails.getPerson();
        return person;
    }*/
}
