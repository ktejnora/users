package com.hubrick.users;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubrick.TejnoraApplication;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * {@link UserRepository} integration test start Spring boot on random port
 * and employs {@link TestRestTemplate} to validate CRUD operations on repository.
 * Validation too.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(TejnoraApplication.class)
@WebIntegrationTest(randomPort = true)
public class UserRepositoryIntegrationTest {

    /**
     * Accepted types, hal reduces code to navigate via HATEOS
     */
    private final List<MediaType> haljson = MediaType.parseMediaTypes("application/hal+json");

    /**
     * UserListResources is type alias used in rest template call
     *
     */
    static class UserListResources extends ParameterizedTypeReference<PagedResources<User>> {
    }

    /**
     * UserResource is type alias used in rest template call
     *
     */
    static class UserResource extends ParameterizedTypeReference<Resource<User>> {
    }

    static class UserBuilder {
        String firstname = "fname";
        String lastname = "lname";
        String email = "lf@email.local";
        String password = "12345678";

        UserBuilder firstname(String firstname) {
            this.firstname = firstname;
            return this;
        }

        UserBuilder lastname(String lastname) {
            this.lastname = lastname;
            return this;
        }

        UserBuilder email(String email) {
            this.email = email;
            return this;
        }

        UserBuilder password(String password) {
            this.password = password;
            return this;
        }

        User toUser() {
            final User user = new User(firstname, lastname, email, password);
            return user;
        }
    }

    @Autowired // or @Inject
    UserRepository userRepo;

    /* Discover port test instance is listening to */
    @Value("${local.server.port}")
    int port;

    RestTemplate rest;

    UserBuilder userBuilder = new UserBuilder();

    /**
     * Set up rest template with HAL json parser
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new Jackson2HalModule());

        final MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(haljson);
        converter.setObjectMapper(mapper);

        rest = new RestTemplate(Collections.<HttpMessageConverter<?>> singletonList(converter));
    }

    @After
    public void cleanUp() throws Exception {
        userRepo.deleteAll();
    }

    @Test
    public void emptyDatabaseReturnsEmptyCollectionOfUsers() throws Exception {
        final ResponseEntity<PagedResources<User>> response =
            rest.exchange("http://localhost:{port}/users", HttpMethod.GET, null, new UserListResources(), port);
        assertThat("List of users on empty database should be empty", response.getBody().getContent(), emptyCollectionOf(User.class));
    }

    @Test
    public void userCreateGetUpdateDeleteLifeCycle() throws Exception {
        final User newUser = userBuilder.toUser();

        final Resource<User> savedUserResource = createUser(newUser);
        final User savedUser = savedUserResource.getContent();
        assertThat("Saved user should not be different from newUser", savedUser, is(equalTo(newUser)));

        final URI userIdURI = baseURI().resolve(savedUserResource.getId().getHref());
        final Resource<User> fetchedUserResource = rest.exchange(userIdURI, HttpMethod.GET, null, new UserResource()).getBody();
        assertThat("Fetched user by id should not be different from savedUser", fetchedUserResource.getContent(), equalTo(savedUser));

        final User updateUser = userBuilder.firstname("spock").toUser();
        final Resource<User> updatedUserResource = rest.exchange(requestEntity(userIdURI, updateUser, HttpMethod.PUT), new UserResource()).getBody();
        assertThat("Updated user should be same requested update", updatedUserResource.getContent(), is(equalTo(updateUser)));

        rest.exchange(userIdURI, HttpMethod.DELETE, null, new UserResource());
        final ResponseEntity<PagedResources<User>> response =
            rest.exchange("http://localhost:{port}/users", HttpMethod.GET, null, new UserListResources(), port);
        assertThat("List of users on empty database should be empty", response.getBody().getContent(), emptyCollectionOf(User.class));
    }

    @Test(expected = HttpServerErrorException.class)
    public void shouldNotCreateUserWithInvalidEmail() throws Exception {
        createUser(userBuilder.email("bad email").toUser());
    }

    @Test(expected = HttpServerErrorException.class)
    public void shouldNotCreateUserWithTooShortPassword() throws Exception {
        createUser(userBuilder.password("1234567").toUser());
    }

    @Test(expected = HttpServerErrorException.class)
    public void shouldNotCreateUserWithEmptyFirstname() throws Exception {
        createUser(userBuilder.firstname("").toUser());
    }

    @Test(expected = HttpServerErrorException.class)
    public void shouldNotCreateUserWithEmptyLastname() throws Exception {
        createUser(userBuilder.lastname("").toUser());
    }

    @Test(expected = HttpServerErrorException.class)
    public void shouldNotCreateUserWithEmptyEmail() throws Exception {
        createUser(userBuilder.email("").toUser());
    }

    @Test(expected = HttpServerErrorException.class)
    public void shouldNotCreateUserWithEmptyPassword() throws Exception {
        createUser(userBuilder.password("").toUser());
    }

    private Resource<User> createUser(User user) {
        return rest.exchange(requestEntity(baseURI(), user, HttpMethod.POST), new UserResource()).getBody();
    }

    private URI baseURI() {
        return new UriTemplate("http://localhost:{port}/users").expand(port);
    }

    private <T> RequestEntity<T> requestEntity(URI uri, T body, HttpMethod method) {
        final RequestEntity<T> request = RequestEntity.method(method, uri).accept(haljson.toArray(new MediaType[0])).body(body);
        return request;
    }

}
