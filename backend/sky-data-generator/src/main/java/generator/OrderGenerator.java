package generator;

import com.github.javafaker.Faker;
import util.BatchInsertUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public class OrderGenerator {

    public record OrderRow(
            String number,
            Integer status,
            Long userId,
            Long addressBookId,
            LocalDateTime orderTime,
            LocalDateTime checkoutTime,
            Integer payMethod,
            Integer payStatus,
            BigDecimal originalAmount,
            Long couponId,
            BigDecimal discountAmount,
            String remark,
            String phone,
            String address,
            String userName,
            String consignee,
            String cancelReason,
            String rejectionReason,
            LocalDateTime cancelTime,
            LocalDateTime estimatedDeliveryTime,
            Integer deliveryStatus,
            LocalDateTime deliveryTime,
            int packAmount,
            int tablewareNumber,
            Integer tablewareStatus,
            BigDecimal amount
    ) {
    }

    private static final String INSERT_SQL = "insert into orders (number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, original_amount, coupon_id, discount_amount, remark, phone, address, user_name, consignee, cancel_reason, rejection_reason, cancel_time, estimated_delivery_time, delivery_status, delivery_time, pack_amount, tableware_number, tableware_status, amount) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    private static final DateTimeFormatter ORDER_NO_TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final Faker faker;

    public OrderGenerator(Locale locale) {
        this.faker = new Faker(locale);
    }

    public List<OrderRow> generateSample(int sampleSize, List<Long> userIds) {
        int size = Math.max(0, sampleSize);
        List<OrderRow> rows = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            rows.add(generateOrderRow(userIds));
        }
        return rows;
    }

    public long generateAndInsert(Connection connection, int count, int batchSize, List<Long> userIds, boolean dryRun) throws SQLException {
        if (count <= 0) {
            return 0;
        }
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize must be positive");
        }
        if (userIds == null || userIds.isEmpty()) {
            throw new IllegalArgumentException("userIds is empty; generate users first or import existing users");
        }

        long inserted = 0;
        List<OrderRow> buffer = new ArrayList<>(Math.min(batchSize, count));
        for (int i = 1; i <= count; i++) {
            buffer.add(generateOrderRow(userIds));
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
                        false,
                        this::bindOrder
                );
                inserted += result.getInserted();
                buffer.clear();
            }
        }

        return inserted;
    }

    private OrderRow generateOrderRow(List<Long> userIds) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        long userId = userIds.get(random.nextInt(userIds.size()));

        LocalDateTime orderTime = LocalDateTime.now().minusDays(random.nextInt(0, 120)).minusMinutes(random.nextInt(0, 24 * 60));
        int status = random.nextInt(1, 7);
        int payStatus = status == 1 ? 0 : 1;
        int payMethod = random.nextBoolean() ? 1 : 2;

        BigDecimal originalAmount = BigDecimal.valueOf(random.nextDouble(15, 240)).setScale(2, RoundingMode.HALF_UP);
        boolean hasCoupon = random.nextInt(0, 10) == 0;
        BigDecimal couponDiscount = hasCoupon ? BigDecimal.valueOf(random.nextDouble(1, 20)).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        if (couponDiscount.compareTo(originalAmount) > 0) {
            couponDiscount = originalAmount.multiply(BigDecimal.valueOf(0.2)).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal discountAmount = originalAmount.subtract(couponDiscount).max(BigDecimal.valueOf(0.01)).setScale(2, RoundingMode.HALF_UP);
        int packAmount = random.nextInt(0, 9);
        BigDecimal amount = discountAmount.add(BigDecimal.valueOf(packAmount)).setScale(2, RoundingMode.HALF_UP);

        LocalDateTime checkoutTime = orderTime.plusMinutes(random.nextInt(1, 40));
        LocalDateTime estimatedDeliveryTime = checkoutTime.plusMinutes(random.nextInt(25, 90));
        LocalDateTime deliveryTime = status == 5 ? estimatedDeliveryTime.plusMinutes(random.nextInt(0, 30)) : null;

        String number = orderTime.format(ORDER_NO_TS) + String.format("%06d", random.nextInt(0, 1_000_000));
        String userName = faker.name().fullName();
        String consignee = faker.name().name();
        String phone = "1" + random.nextInt(3, 10) + String.format("%09d", random.nextInt(0, 1_000_000_000));
        String address = faker.address().fullAddress();
        String remark = random.nextInt(0, 4) == 0 ? null : faker.lorem().sentence(6);

        String cancelReason = status == 6 ? faker.lorem().sentence(4) : null;
        String rejectionReason = status == 6 && random.nextInt(0, 10) == 0 ? faker.lorem().sentence(4) : null;
        LocalDateTime cancelTime = status == 6 ? checkoutTime.plusMinutes(random.nextInt(1, 30)) : null;

        int deliveryStatus = random.nextBoolean() ? 1 : 0;
        int tablewareNumber = random.nextInt(1, 6);
        int tablewareStatus = random.nextBoolean() ? 1 : 0;

        Long couponId = hasCoupon ? (long) random.nextInt(1, 1000) : null;
        Long addressBookId = (long) random.nextInt(0, 5);

        return new OrderRow(
                number,
                status,
                userId,
                addressBookId,
                orderTime,
                checkoutTime,
                payMethod,
                payStatus,
                originalAmount,
                couponId,
                discountAmount,
                remark,
                phone,
                address,
                userName,
                consignee,
                cancelReason,
                rejectionReason,
                cancelTime,
                estimatedDeliveryTime,
                deliveryStatus,
                deliveryTime,
                packAmount,
                tablewareNumber,
                tablewareStatus,
                amount
        );
    }

    private void bindOrder(PreparedStatement ps, OrderRow row) throws SQLException {
        ps.setString(1, row.number());
        ps.setInt(2, row.status());
        ps.setLong(3, row.userId());
        if (row.addressBookId() == null) {
            ps.setNull(4, Types.BIGINT);
        } else {
            ps.setLong(4, row.addressBookId());
        }
        ps.setTimestamp(5, Timestamp.valueOf(row.orderTime()));
        ps.setTimestamp(6, Timestamp.valueOf(row.checkoutTime()));
        ps.setInt(7, row.payMethod());
        ps.setInt(8, row.payStatus());
        ps.setBigDecimal(9, row.originalAmount());
        if (row.couponId() == null) {
            ps.setNull(10, Types.BIGINT);
        } else {
            ps.setLong(10, row.couponId());
        }
        ps.setBigDecimal(11, row.discountAmount());
        ps.setString(12, row.remark());
        ps.setString(13, row.phone());
        ps.setString(14, row.address());
        ps.setString(15, row.userName());
        ps.setString(16, row.consignee());
        ps.setString(17, row.cancelReason());
        ps.setString(18, row.rejectionReason());
        if (row.cancelTime() == null) {
            ps.setNull(19, Types.TIMESTAMP);
        } else {
            ps.setTimestamp(19, Timestamp.valueOf(row.cancelTime()));
        }
        ps.setTimestamp(20, Timestamp.valueOf(row.estimatedDeliveryTime()));
        ps.setInt(21, row.deliveryStatus());
        if (row.deliveryTime() == null) {
            ps.setNull(22, Types.TIMESTAMP);
        } else {
            ps.setTimestamp(22, Timestamp.valueOf(row.deliveryTime()));
        }
        ps.setInt(23, row.packAmount());
        ps.setInt(24, row.tablewareNumber());
        ps.setInt(25, row.tablewareStatus());
        ps.setBigDecimal(26, row.amount());
    }
}
