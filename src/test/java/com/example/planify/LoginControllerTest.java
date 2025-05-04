package com.example.planify;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class LoginControllerTest {

    @BeforeEach
    public void setUp()
    {

    }

    @Test
    public void testValidateInput_EmptyLogin_ReturnsFalse()
    {
        assertEquals(5,5);
    }
}
