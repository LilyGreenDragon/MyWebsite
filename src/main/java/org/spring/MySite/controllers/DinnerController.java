package org.spring.MySite.controllers;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.spring.MySite.models.Dinner;
import org.spring.MySite.models.Element;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
    @Controller
    //@RequestMapping("/make")
    public class DinnerController {

    List<Element> elementsBasic;
    Dinner dinnerOrder;


        @GetMapping("/makeDinner")
        public String showDinnerForm(Model model) {
            elementsBasic = Arrays.asList(
                    new Element("NSSP", "Noodle soup", Element.Type.SOUP),
                    new Element("VSSP", "Vegetable soup",Element.Type.SOUP),
                    new Element("BSDH", "Beef stew", Element.Type.DISH),
                    new Element("CDH", "Chicken", Element.Type.DISH),
                    new Element("FPGH", "Fried potato", Element.Type.GARNISH),
                    new Element("SGH", "Spaghetti", Element.Type.GARNISH),
                    new Element("RVGH", "Rice with vegetables", Element.Type.GARNISH),
                    new Element("CSSE", "Cheese sauce", Element.Type.SAUCE),
                    new Element("TSSE", "Tomato sauce", Element.Type.SAUCE)
            );
            Element.Type[] types = Element.Type.values();
            for (Element.Type type : types) {
                model.addAttribute(type.toString().toLowerCase(),
                        filterByType(elementsBasic, type));
            }
            model.addAttribute("dinner", new Dinner());

            return "makeDinner";
        }

    private List<Element> filterByType(List<Element> elements, Element.Type type) {

        return elements.stream()
                .filter(x -> x.getType().equals(type))
                .collect(Collectors.toList());
    }

    @PostMapping("/makeDinner")
    public String processDesign(@ModelAttribute("dinner") @Valid Dinner dinner, @RequestParam(value = "selected", required = false) String[] selected, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
        return "makeDinner";
    }
            List<Element> elementsDinner=new ArrayList<>();
            List<String> listId = Arrays.asList(selected);
            for (String id : listId){
                for (Element el : elementsBasic){
                    if (id.equals(el.getId())) {
                        elementsDinner.add(el);
                    }
                }
            }

        dinner.setElementsDinner(elementsDinner);
            dinnerOrder=dinner;
        // Save the taco design...
        // We'll do this in chapter 3
        log.info("Processing design: " + dinner);
        return "orderOk";
    }



    }

