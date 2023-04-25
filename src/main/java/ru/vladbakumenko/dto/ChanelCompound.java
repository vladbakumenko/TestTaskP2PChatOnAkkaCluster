package ru.vladbakumenko.dto;


import java.util.Objects;

public class ChanelCompound {
    String member1;
    String member2;

    public ChanelCompound(String member1, String member2) {
        this.member1 = member1;
        this.member2 = member2;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChanelCompound chanel = (ChanelCompound) o;
        return (Objects.equals(member1, chanel.member1) && Objects.equals(member2, chanel.member2)) ||
                (Objects.equals(member2, chanel.member1) && Objects.equals(member1, chanel.member2)) ||
                (Objects.equals(member1, chanel.member2) && Objects.equals(member2, chanel.member1));
    }

    @Override
    public int hashCode() {
        return member1.hashCode() + member2.hashCode();
    }
}
