package model;

public class Member {

    private int member_id;
    private String name;
    private String email;
    private String phone;
    private String joinDate;

    public Member(int member_id, String name, String email, String phone, String joinDate) {
        this.member_id = member_id;
        this.name      = name;
        this.email     = email;
        this.phone     = phone;
        this.joinDate  = joinDate;
    }

    public int    getMember_id() { return member_id; }
    public String getName()      { return name; }
    public String getEmail()     { return email; }
    public String getPhone()     { return phone; }
    public String getJoinDate()  { return joinDate; }
}