package org.spring.MySite.controllers;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.spring.MySite.models.Person;
import org.spring.MySite.repositories.PeopleRepository;
import org.spring.MySite.security.P;
import org.spring.MySite.security.PersonDetails;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Controller
public class PeopleController {

    private PeopleRepository peopleRepository;

    private JavaMailSender mailSender;
    private SessionRegistry sessionRegistry;

    @Autowired
    public PeopleController(PeopleRepository peopleRepository, JavaMailSender mailSender, SessionRegistry sessionRegistry) {
        this.peopleRepository = peopleRepository;
        this.mailSender = mailSender;
        this.sessionRegistry = sessionRegistry;
    }

    @GetMapping()
    public String firstPage(Authentication authentication, Model model) {
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
    public String homePage( Model model, @P Person personLogged) {

        model.addAttribute("person", personLogged);
        return "index";
    }

    @GetMapping("/myPage")
    public String myPage(Model model, @P Person personLogged) {

        model.addAttribute("person", personLogged);
        return "indexMyPage";
    }

    @GetMapping("/photo")
    public String photoPage() {
        return "indexMyPhoto";
    }

    @GetMapping("/news")
    public String newsPage() {
        return "indexNews";
    }

    @GetMapping("/holiday")
    public String holidayPage() {
        return "indexMyHoliday";
    }

    @GetMapping("/myPage/photo")
    public String photoNew(Model model, @P Person personLogged) {
        model.addAttribute("person", personLogged);
        return "photo";
    }

    @DeleteMapping()
    public String deletePerson(@P Person personLogged) {

        peopleRepository.deleteById(personLogged.getId());
       // SecurityContextHolder.clearContext();

        List<Object> principals = sessionRegistry.getAllPrincipals();
        for (Object principal : principals) {
            List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);
            for (SessionInformation sessionInfo : sessions) {
                if (SecurityContextHolder.getContext().getAuthentication().getPrincipal().equals(sessionInfo.getPrincipal())) {
                    if (!sessionInfo.isExpired()) sessionInfo.expireNow();
                    sessionRegistry.removeSessionInformation(sessionInfo.getSessionId());
                }
            }
        }
        SecurityContextHolder.getContext().setAuthentication(null);

        return "redirect:/";
    }

    @PostMapping("/home")
    public String imageTheme(@ModelAttribute("person") Person person, @P Person updatedPerson) {
        updatedPerson.setImageTheme(person.getImageTheme());
        peopleRepository.save(updatedPerson);
        return "redirect:/home";
    }

    @PostMapping("/myPage")
    public String update(@ModelAttribute("person") Person person, @P Person updatedPerson) {
        updatedPerson.setName(person.getName());
        updatedPerson.setSurname(person.getSurname());
        updatedPerson.setBirthdate(person.getBirthdate());
        peopleRepository.save(updatedPerson);
        return "redirect:/myPage";
    }

    @PostMapping("/myPage/mail")
    public String eMail(@ModelAttribute("person") Person person, @P Person personMail) {

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
        String htmlMsg = person.getMessage();

        try {
            helper.setText(htmlMsg, true);
            helper.setTo("");
            helper.setSubject("Сообщение от " + personMail.getUsername()+ " "+ personMail.getEmail());
            helper.setFrom("");
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        mailSender.send(mimeMessage);

        return "redirect:/myPage";
    }

    @PostMapping("/myPage/photo/delete")
    public String deletePhoto(@ModelAttribute("person") Person person, @P Person updatedPerson) {
        updatedPerson.setPhoto(person.getPhoto());
        peopleRepository.save(updatedPerson);
        return "redirect:/myPage";
    }

    @PostMapping("/myPage/photo")
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
                //System.out.println(uuidAsString);


               /* File filepath = new File("C:\\progJava\\MySite\\MySite\\src\\main\\resources\\static\\images\\imagecab\\");//Создать папку
                if (!filepath.exists()) {// Если папки нет, создайте новую
                    filepath.mkdirs();
                } */

                File picfilepath = new File("C:\\progJava\\MySite\\MySite\\src\\main\\resources\\static\\images\\imagecab\\" + uuidAsString + ".png");// сохранить документ

                final String uploadLocation = getClass().getClassLoader().getResource("static").toString();
                //we should get rid of file:/, hence the substring
                Path uploadDirectory = Paths.get(uploadLocation.substring(6, uploadLocation.length()));

                File picfilepathTarget = new File(uploadDirectory + "\\images\\imagecab\\" + uuidAsString + ".png");


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
                peopleRepository.save(updatedPerson);

            } else {

                //String path = "D:images\\upload_final\\030311175258.jpg";

                String[] stringsAfterSplit2 = person.getPhoto().split("/");
                String str0 = stringsAfterSplit2[3];
                String[] stringsAfterSplit3 = str0.split(".");
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

        double rateX = (double)image.getWidth() / Integer.parseInt(widthImage);

       /* System.out.println(xx);
        System.out.println(yy);
        System.out.println(ww);
        System.out.println(hh);
        System.out.println(image.getWidth());
        System.out.println(Integer.parseInt(widthImage));
        System.out.println(rateX);*/

        BufferedImage outB = image.getSubimage((int)(xx * rateX), (int)(yy * rateX), (int)(ww * rateX), (int)(hh * rateX));

        final String uploadLocation = getClass().getClassLoader().getResource("static").toString();
        //we should get rid of file:/, hence the substring
        Path uploadDirectory = Paths.get(uploadLocation.substring(6, uploadLocation.length()));

        try {
            ImageIO.write(outB, "png", new File("C:\\progJava\\MySite\\MySite\\src\\main\\resources\\static\\images\\imagecab\\" + str));
            ImageIO.write(outB, "png", new File(uploadDirectory + "\\images\\imagecab\\" + str));

        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }

    }


}
