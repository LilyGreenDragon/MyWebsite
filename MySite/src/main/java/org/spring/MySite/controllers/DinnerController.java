package org.spring.MySite.controllers;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.spring.MySite.models.Dinner;
import org.spring.MySite.models.Element;
import org.spring.MySite.models.Order;
import org.spring.MySite.models.Person;
import org.spring.MySite.security.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
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
    List<Order> orders = new ArrayList<>();

        @GetMapping("/make")
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

    @PostMapping("/make")
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

        log.info("Processing design: " + dinner);
        return "redirect:/orders/current";
    }

    @GetMapping("/orders/current")
    public String orderForm(Model model) {
        model.addAttribute("order", new Order());
        return "orderForm";
    }

    @PostMapping("/orders")
    public String processOrder(@ModelAttribute("order") @Valid Order order, BindingResult bindingResult, @P Person personLogged) {
        if (bindingResult.hasErrors()) {
            return "orderForm";
        }
        order.setDinner(dinnerOrder);
        order.setPerson(personLogged);
        log.info("Order submitted: " + order);
orders.add(order);
        return "redirect:/orderOk";
    }

    @GetMapping("/show")
    public String showOrders(Model model) {
        model.addAttribute("orders", orders);
            return "showOrders";
    }

    }

