package org.mvel3;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Person {

    private String name;
    private int age;

    public int publicAge;

    private Person parent;

    public Person parentPublic;

    private Address address;
    private List<Address> addresses = new ArrayList<>();
    private Gender gender;
    public String nickName;

    private Map<String, String> items = new HashMap<>();

    private Map<String, Integer> prices = new HashMap<>();

    private BigDecimal salary;

    private Integer ageAsInteger;

    private BigInteger ageAsBigInteger;

    public int[] publicIntArray;

    public BigInteger publicBigInt;

    public BigDecimal publicBigDec;

    public Map<String, BigDecimal> publicMapBigDec;

    public List<BigDecimal> publicListBigDec;

    public BigDecimal[] publicArrayBigDec;

    public Person(String name) {
        this(name, null);
    }


    public Person(String name, Person parent) {
        this(name, parent, Gender.NOT_AVAILABLE);
    }

    public Person(String name, Person parent, Gender gender) {
        this.name = name;
        this.parent = parent;
        this.gender = gender;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Person getParent() {
        return parent;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Gender getGender() {
        return gender;
    }


    public Person setGender(Gender gender) {
        this.gender = gender;
        return this;
    }

    public Map<String, String> getItems() {
        return items;
    }

    public void setItems(Map<String, String> items) {
        this.items = items;
    }

    public Map<String, Integer> getPrices() {
        return prices;
    }

    public void setPrices(Map<String, Integer> prices) {
        this.prices = prices;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary( BigDecimal salary ) {
        this.salary = salary;
    }

    public Integer getAgeAsInteger() {
        return ageAsInteger;
    }

    public void setAgeAsInteger( Integer ageAsInteger ) {
        this.ageAsInteger = ageAsInteger;
    }

    public void setAgeAsBigInteger( BigInteger ageAsBigInteger ) {
        this.ageAsBigInteger = ageAsBigInteger;
    }

    public BigInteger getAgeAsBigInteger() {
        return this.ageAsBigInteger;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    public List<String> getStreets() {
        return null;
    }

    public String[] getRoads() {
        return null;
    }

    public static boolean isEven(int value) {
        return true;
    }

    public Map<String, BigInteger> getBigIntegerMap() {
        return null;
    }

    public Map<String, BigDecimal> getBigDecimalMap() {
        return null;
    }

    public void process1(String a, String b, String c, int... nums) {}

    public void process2(int a, int b, int c, String... str) {}
}