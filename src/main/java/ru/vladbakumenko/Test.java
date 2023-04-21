package ru.vladbakumenko;

import ru.vladbakumenko.model.ChanelCompound;

public class Test {
    public static void main(String[] args) {
        ChanelCompound compound1 = new ChanelCompound("Vlad", "Dasha");
        ChanelCompound compound2 = new ChanelCompound("Dasha", "Vlad");

        System.out.println(compound1.equals(compound2));
        System.out.println(compound1.hashCode() == compound2.hashCode());
    }
}
