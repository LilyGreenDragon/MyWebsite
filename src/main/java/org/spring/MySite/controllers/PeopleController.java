package org.spring.MySite.controllers;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;


import jakarta.validation.Valid;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.spring.MySite.models.Person;
import org.spring.MySite.repositories.PeopleRepository;
import org.spring.MySite.security.P;
import org.spring.MySite.security.PersonDetails;
import org.spring.MySite.services.PeopleService;
import org.spring.MySite.util.PersonErrorResponse;
import org.spring.MySite.util.PersonNotCreatedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class PeopleController {

    private PeopleService peopleService;
    private JavaMailSender mailSender;
    private SessionRegistry sessionRegistry;

    @Value("${pathToDirectory:/home/karina/ProgJava/imagecab/}")
    //@Value("${pathToDirectory:/app/imagecab/}")
    String pathToDirectory;

    @Autowired
    public PeopleController(PeopleService peopleService, JavaMailSender mailSender, SessionRegistry sessionRegistry) {
        this.peopleService = peopleService;
        this.mailSender = mailSender;
        this.sessionRegistry = sessionRegistry;
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

        if ((!(authentication instanceof AnonymousAuthenticationToken)) && authentication != null) {

            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            for (GrantedAuthority grantedAuthority : authorities) {
                if (grantedAuthority.getAuthority().equals("BLOCKED")) {
                    return "blockPage";
                }
            }
                return "redirect:/home";
            }
            return "first";
        }


    @GetMapping("/home")
    public String showHome( Model model, @P Person personLogged) {
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

    @DeleteMapping()
    public String deletePerson(@P Person personLogged) {

        peopleService.deleteById(personLogged.getId());
       // SecurityContextHolder.clearContext();

        /*List<Object> principals = sessionRegistry.getAllPrincipals();
        for (Object principal : principals) {
            List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);
            for (SessionInformation sessionInfo : sessions) {
                if (SecurityContextHolder.getContext().getAuthentication().getPrincipal().equals(sessionInfo.getPrincipal())) {
                    if (!sessionInfo.isExpired()) sessionInfo.expireNow();
                    sessionRegistry.removeSessionInformation(sessionInfo.getSessionId());
                    }
                    */

        PersonDetails personDetails = new PersonDetails(personLogged);
        List<SessionInformation> sessions = sessionRegistry.getAllSessions(personDetails, false);
        for (SessionInformation sessionInfo : sessions) {
                sessionInfo.expireNow();
                sessionRegistry.removeSessionInformation(sessionInfo.getSessionId());
        }

        SecurityContextHolder.getContext().setAuthentication(null);
        return "redirect:/";
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
                            @RequestParam("x") String x,  @RequestParam("y") String y,
                            @RequestParam("w") String w, @RequestParam("h") String h, @RequestParam("widthImage") String widthImage ) {

        //String pathToDirectory = "C:\\cab\\imagecab\\";
        //String pathToDirectory = "/app/imagecab/";

        System.out.println("person.getPhoto() " +person.getPhoto());
        if (!person.getPhoto().isEmpty()) {

            String[] stringsAfterSplit = person.getPhoto().split(",");
            //System.out.println("stringsAfterSplit " +stringsAfterSplit);

            if (stringsAfterSplit.length != 1) {

                String base64 = stringsAfterSplit[1];// Преобразование изображения через base64, удаление заголовка изображения (data: image / jpg; base64,)
                //System.out.println("base64 " +base64);
                // 2, декодировать в байтовый массив

                byte[] data = Base64.getDecoder().decode(base64);
                //System.out.println("data " +data);
                // 3, файл байтового потока

                UUID uuid = UUID.randomUUID();
                String uuidAsString = uuid.toString();// uuid как имя файла при сохранении
                //System.out.println("uuidAsString " +uuidAsString);

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
                //System.out.println("stringsAfterSplit2 " +stringsAfterSplit2[2]);
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
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
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
