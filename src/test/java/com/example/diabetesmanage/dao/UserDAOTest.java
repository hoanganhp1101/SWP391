package com.example.diabetesmanage.dao;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserDAOTest {
    @Test
    void sha256HashMatchesKnownValueAndHandlesNull() {
        UserDAO dao = new UserDAO();
        assertEquals("2bb80d537b1da3e38bd30361aa855686bde0eacd7162fef6a25fe97bf527a25b", dao.hashSHA256("secret"));
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", dao.hashSHA256(null));
    }
}
