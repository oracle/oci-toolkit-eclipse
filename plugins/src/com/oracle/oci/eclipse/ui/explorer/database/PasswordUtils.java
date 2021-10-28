/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Text;

import com.oracle.oci.eclipse.Activator;
import com.oracle.oci.eclipse.ui.explorer.database.validate.InputValidator;
import com.oracle.oci.eclipse.ui.explorer.database.validate.PasswordInputValidator;

public class PasswordUtils {

    public static MultiStatus validate(Text adminPasswordText, Text confirmPasswordText) {
        MultiStatus multiStatus = new MultiStatus(Activator.PLUGIN_ID, 0, "", null);
        IStatus status = validatePasswords(adminPasswordText, confirmPasswordText);
        if (!status.isOK()) {
            multiStatus.add(status);
        }
        return multiStatus;
    }

    private static final InputValidator<String> passwordInputValidator = new InputValidator<String>() {
        @Override
        public IStatus validate(String inputValue) {
            if (inputValue == null || inputValue.trim().isEmpty()) {
                return error("Passwords can't be empty");
            }
            if (inputValue.length() < 12 || inputValue.length() > 30) {
                return error("Passwords must be between 12 and 30 characters long");
            }
            boolean containsAtLeastOneUpper = false;
            boolean containsAtLeastOneLower = false;
            boolean containsAtLeastOneNumber = false;
            for (char character : inputValue.toCharArray()) {
                if (Character.isUpperCase(character)) {
                    containsAtLeastOneUpper = true;
                } else if (Character.isLowerCase(character)) {
                    containsAtLeastOneLower = true;
                } else if (Character.isDigit(character)) {
                    containsAtLeastOneNumber = true;
                } else {
                    if (character == '"') {
                        return error("Passwords must be between 12 and 30 characters long");
                    }
                }
            }
            if (!containsAtLeastOneLower) {
                return error("Password must contain at least one lower case character");
            }
            if (!containsAtLeastOneUpper) {
                return error("Password must contain at least one upper case character");
            }
            if (!containsAtLeastOneNumber) {
                return error("Password must contain at least one number");
            }
            if (inputValue.toLowerCase().contains("admin")) {
                return error("Passwords cannot contain the word 'admin' in any case");
            }
            return Status.OK_STATUS;
        }

    };

    private final static PasswordInputValidator passwordValidator = new PasswordInputValidator(passwordInputValidator);

    public static IStatus validatePasswords(Text adminPasswordText, Text confirmAdminPasswordText) {
        IStatus status = validatePassword(adminPasswordText);
        if (!status.isOK()) {
            return status;
        }
        String password = adminPasswordText.getText();
        String confirmPassword = confirmAdminPasswordText.getText();
        if (!password.equals(confirmPassword)) {
            return error("Confirmed password must match password");
        }
        return Status.OK_STATUS;
    }

    private static IStatus validatePassword(Text source) {
        return passwordValidator.validate(source);
    }

    private final static List<Character> PASSWORD_CHARS;
    private final static List<Character> LOWER_CASE = new ArrayList<>(26);
    private final static List<Character> UPPER_CASE = new ArrayList<>(26);
    private final static List<Character> DIGITS = new ArrayList<>(10);
    private final static List<?> AT_LEAST_ONE[] = {LOWER_CASE, UPPER_CASE, DIGITS};

    static {
        List<Character> characters = new ArrayList<>();
        for (int i = (int) 'a'; i <= (int) 'z'; i++) {
            LOWER_CASE.add((char) i);
        }

        for (int i = (int) 'A'; i <= (int) 'Z'; i++) {
            UPPER_CASE.add((char) i);
        }

        for (int i = (int) '0'; i <= (int) '9'; i++) {
            DIGITS.add((char) i);
        }

        characters.addAll(LOWER_CASE);
        characters.addAll(UPPER_CASE);
        characters.addAll(DIGITS);
        characters.addAll(Arrays.asList('!', '@', '#','$', '%', '^', '&', '*', '(', ')', '+', '=', '-', '?'));

        PASSWORD_CHARS = Collections.unmodifiableList(characters);
    }

    public static String generateAdminPassword() {
        final int minSize = 12;
        final int maxSize = 30;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int length = random.nextInt(minSize, maxSize + 1);
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < length; i++)
        {
            int nextInt = random.nextInt(0, PASSWORD_CHARS.size());
            password.append(PASSWORD_CHARS.get(nextInt));
        }
        
        // get a list of three unique random indices that are within the
        // password length
        List<Integer>  checkOffsets = new ArrayList<>();
        while(checkOffsets.size() < 3)
        {
            int nextInt = random.nextInt(0, length);
            if (!checkOffsets.contains(nextInt)) {
                checkOffsets.add(nextInt);
            }
        }
        
        // for each offset, check the character at that offset.  If it's
        // not one of the mandatory ones, replace it.
        for (int i = 0; i < 3; i++)
        {
            Integer index = checkOffsets.get(i);
            char charAt = password.charAt(index);
            List<?> allowed = AT_LEAST_ONE[i];
            if (!allowed.contains(charAt))
            {
                int nextInt = random.nextInt(0, allowed.size());
                @SuppressWarnings("unchecked")
                Character allowedChar = ((List<Character>)allowed).get(nextInt);
                password.setCharAt(index, allowedChar);
            }
        }
        return password.toString();
    }

    private static IStatus error(String message) {
        return new Status(IStatus.ERROR, Activator.PLUGIN_ID, message);
    }
}
