package org.kvk.server.classes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
//import org.hibernate.annotations.GeneratorType;

import javax.persistence.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "fix_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    @Column(name = "firstname")
    private String firstName;
    @Column(name = "lastname")
    private String lastName;

    @Override
    public String toString(){
        return "firstname: " + firstName + "; lastname:" + lastName;
    }

    public static  User form(UserForm form){
        return User.builder()
                .firstName(form.getFirstname())
                .lastName(form.getLastname())
                .build();
    }


}
