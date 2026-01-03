package com.combiphar.core.util;

import com.combiphar.core.repository.UserRepository;

/**
 * Utility untuk operasi customer. Single Responsibility: helper methods untuk
 * customer-related operations.
 */
public class CustomerUtil {

    private static final UserRepository userRepository = new UserRepository();

    /**
     * Mengambil nama customer dari user ID.
     *
     * @param userId ID user
     * @return nama customer atau default "Customer"
     */
    public static String getCustomerName(String userId) {
        if (userId == null || userId.isBlank()) {
            return "Customer";
        }
        return userRepository.findById(userId)
                .map(user -> user.getName())
                .orElse("Customer #" + userId.substring(0, Math.min(8, userId.length())));
    }
}
