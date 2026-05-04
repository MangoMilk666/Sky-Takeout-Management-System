package generator;

import com.github.javafaker.Faker;
import util.BatchInsertUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class UserGenerator {

    public record UserRow(
            String openid,
            String name,
            String phone,
            String sex,
            String idNumber,
            String avatar,
            LocalDateTime createTime
    ) {
    }

    private static final String INSERT_SQL = "insert into user(openid, name, phone, sex, id_number, avatar, create_time) values (?,?,?,?,?,?,?)";

    private final Faker faker;

    public UserGenerator(Locale locale) {
        this.faker = new Faker(locale);
    }

    public List<UserRow> generateSample(int sampleSize) {
        int size = Math.max(0, sampleSize);
        List<UserRow> rows = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            rows.add(generateUserRow());
        }
        return rows;
    }

    public List<Long> generateAndInsert(Connection connection, int count, int batchSize, boolean dryRun) throws SQLException {
        if (count <= 0) {
            return List.of();
        }
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize must be positive");
        }

        List<Long> userIds = new ArrayList<>(count);
        List<UserRow> buffer = new ArrayList<>(Math.min(batchSize, count));
        for (int i = 1; i <= count; i++) {
            buffer.add(generateUserRow());
            if (buffer.size() >= batchSize || i == count) {
                if (dryRun) {
                    buffer.clear();
                    continue;
                }
                BatchInsertUtil.BatchInsertResult result = BatchInsertUtil.batchInsert(
                        connection,
                        INSERT_SQL,
                        buffer,
                        batchSize,
                        true,
                        this::bindUser
                );
                userIds.addAll(result.getGeneratedIds());
                buffer.clear();
            }
        }
        return userIds;
    }

    private UserRow generateUserRow() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String openid = UUID.randomUUID().toString().replace("-", "");
        String name = faker.name().fullName();
        String phone = "1" + random.nextInt(3, 10) + String.format("%09d", random.nextInt(0, 1_000_000_000));
        String sex = random.nextBoolean() ? "1" : "0";
        String idNumber = String.format("%06d%08d%03dX", random.nextInt(100000, 999999), random.nextInt(19700101, 20051231), random.nextInt(100, 999));
        String avatar = "https://example.com/avatar/" + openid.substring(0, 12) + ".png";
        LocalDateTime createTime = LocalDateTime.now().minusDays(random.nextInt(0, 365)).minusMinutes(random.nextInt(0, 24 * 60));
        return new UserRow(openid, name, phone, sex, idNumber, avatar, createTime);
    }

    private void bindUser(PreparedStatement ps, UserRow row) throws SQLException {
        ps.setString(1, row.openid());
        ps.setString(2, row.name());
        ps.setString(3, row.phone());
        ps.setString(4, row.sex());
        ps.setString(5, row.idNumber());
        ps.setString(6, row.avatar());
        ps.setTimestamp(7, Timestamp.valueOf(row.createTime()));
    }
}
