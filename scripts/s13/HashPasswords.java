import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
class HashPasswords {
    public static void main(String[] args) {
        var encoder = new BCryptPasswordEncoder();
        for (String name : new String[]{"WRITER_PASSWORD", "READER_PASSWORD"}) {
            String password = System.getenv(name);
            if (password == null || password.isBlank()) throw new IllegalStateException("Falta " + name);
            System.out.println(encoder.encode(password));
        }
    }
}
