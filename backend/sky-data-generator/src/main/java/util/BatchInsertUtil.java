package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BatchInsertUtil {

    @FunctionalInterface
    public interface StatementBinder<T> {
        void bind(PreparedStatement ps, T item) throws SQLException;
    }

    public static final class BatchInsertResult {
        private final long inserted;
        private final List<Long> generatedIds;

        public BatchInsertResult(long inserted, List<Long> generatedIds) {
            this.inserted = inserted;
            this.generatedIds = generatedIds;
        }

        public long getInserted() {
            return inserted;
        }

        public List<Long> getGeneratedIds() {
            return generatedIds;
        }
    }

    public static <T> BatchInsertResult batchInsert(
            Connection connection,
            String sql,
            List<T> items,
            int batchSize,
            boolean returnGeneratedKeys,
            StatementBinder<T> binder
    ) throws SQLException {
        if (items == null || items.isEmpty()) {
            return new BatchInsertResult(0, Collections.emptyList());
        }
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize must be positive");
        }

        boolean originalAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);

        long inserted = 0;
        List<Long> generatedIds = returnGeneratedKeys ? new ArrayList<>(items.size()) : Collections.emptyList();

        try (PreparedStatement ps = connection.prepareStatement(
                sql,
                returnGeneratedKeys ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS
        )) {
            int currentBatchCount = 0;
            for (T item : items) {
                binder.bind(ps, item);
                ps.addBatch();
                currentBatchCount++;

                if (currentBatchCount >= batchSize) {
                    inserted += executeBatch(ps, returnGeneratedKeys, generatedIds);
                    currentBatchCount = 0;
                }
            }
            if (currentBatchCount > 0) {
                inserted += executeBatch(ps, returnGeneratedKeys, generatedIds);
            }

            connection.commit();
            return new BatchInsertResult(inserted, generatedIds);
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(originalAutoCommit);
        }
    }

    private static long executeBatch(PreparedStatement ps, boolean returnGeneratedKeys, List<Long> generatedIds) throws SQLException {
        int[] results = ps.executeBatch();
        long inserted = 0;
        for (int r : results) {
            if (r == Statement.SUCCESS_NO_INFO) {
                inserted += 1;
            } else if (r > 0) {
                inserted += r;
            }
        }

        if (returnGeneratedKeys) {
            try (ResultSet rs = ps.getGeneratedKeys()) {
                while (rs.next()) {
                    generatedIds.add(rs.getLong(1));
                }
            }
        }

        ps.clearBatch();
        return inserted;
    }
}
