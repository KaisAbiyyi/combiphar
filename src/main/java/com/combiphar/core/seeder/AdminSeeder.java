package com.combiphar.core.seeder;

import org.mindrot.jbcrypt.BCrypt;

import com.combiphar.core.config.DatabaseConfig;
import com.combiphar.core.model.Role;
import com.combiphar.core.model.User;
import com.combiphar.core.repository.UserRepository;

/**
 * Seeder for creating admin account.
 * Run with: gradle seed
 */
public class AdminSeeder {

    public static void main(String[] args) {
        System.out.println("=====================================");
        System.out.println("   Combiphar Admin Seeder");
        System.out.println("=====================================\n");

        try {
            // Initialize database connection
            DatabaseConfig.getConnection().close(); // Just to test connection
            System.out.println("✓ Database connection successful\n");

            UserRepository userRepository = new UserRepository();
            
            // Define admin credentials
            String adminEmail = "admin@combiphar.com";
            String adminPassword = "Admin123456";
            String adminName = "Admin Combiphar";
            
            // Check if admin already exists
            if (userRepository.findByEmail(adminEmail).isPresent()) {
                System.out.println("⚠ Admin user already exists!");
                System.out.println("Email: " + adminEmail);
                System.exit(0);
            }

            // Create admin user
            String hashedPassword = BCrypt.hashpw(adminPassword, BCrypt.gensalt());
            User admin = new User(null, adminName, adminEmail, hashedPassword, Role.ADMIN);
            admin.setPhone("021-1234567");
            admin.setAddress("Jakarta, Indonesia");
            admin.setStatus("ACTIVE");
            
            userRepository.save(admin);
            
            System.out.println("✓ Admin account created successfully!\n");
            System.out.println("Login Credentials:");
            System.out.println("  Email    : " + adminEmail);
            System.out.println("  Password : " + adminPassword);
            System.out.println("\n✓ Login URL: http://localhost:7070/admin/login");
            System.out.println("\n⚠ IMPORTANT: Please change the password after first login!");
            System.out.println("=====================================\n");

        } catch (Exception e) {
            System.err.println("❌ Error creating admin account:");
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
