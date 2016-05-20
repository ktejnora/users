package com.hubrick.users;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * User Entity<br />
 * Primary key is generated as the user can change email address.<br />
 * Email is unique amongst all users.<br />
 * Password is expected to be Base64 encoded strong hash salted with email.
 */
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotEmpty
    private String firstname;

    @NotEmpty
    private String lastname;

    @Email
    @Column(unique = true)
    @NotEmpty
    private String email;

    /**
     * Password is base64 encoded 256bit or stronger hashed and salted with email
     */
    @NotNull
    @Length(min = 8)
    private String password;

    /**
     * No-arg constuctor
     */
    public User() {
    }

    /**
     * Creates User with all properties except id.
     *
     * @param firstname
     * @param lastname
     * @param email
     * @param password
     */
    public User(String firstname, String lastname, String email, String password) {
        super();
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.password = password;
    }

    /**
     * Creates User with all properties passed to constructor
     *
     * @param id
     * @param firstname
     * @param lastname
     * @param email
     * @param password
     */
    public User(Long id, String firstname, String lastname, String email, String password) {
        super();
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return String.format("User [id=%s, firstname=%s, lastname=%s, email=%s, password=%s]", id, firstname, lastname, email,
            (password != null ? "***" : ""));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((email == null) ? 0 : email.hashCode());
        result = prime * result + ((firstname == null) ? 0 : firstname.hashCode());
        result = prime * result + ((lastname == null) ? 0 : lastname.hashCode());
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final User other = (User) obj;
        if (email == null) {
            if (other.email != null) {
                return false;
            }
        } else if (!email.equals(other.email)) {
            return false;
        }
        if (firstname == null) {
            if (other.firstname != null) {
                return false;
            }
        } else if (!firstname.equals(other.firstname)) {
            return false;
        }
        if (lastname == null) {
            if (other.lastname != null) {
                return false;
            }
        } else if (!lastname.equals(other.lastname)) {
            return false;
        }
        if (password == null) {
            if (other.password != null) {
                return false;
            }
        } else if (!password.equals(other.password)) {
            return false;
        }
        return true;
    }

}
