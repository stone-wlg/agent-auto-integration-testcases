package test.apache.skywalking.testcase.cassandra.controller;

import com.datastax.driver.core.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/case")
public class CaseController {

    private static final String CREATE_KEYSPACE_SQL = "CREATE KEYSPACE IF NOT EXISTS demo WITH replication = " +
        "{'class': 'SimpleStrategy', 'replication_factor': 1}";
    private static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS demo.test(id TEXT PRIMARY KEY, value TEXT)";
    private static final String INSERT_DATA_SQL = "INSERT INTO demo.test(id, value) VALUES(?,?)";
    private static final String SELECT_DATA_SQL = "SELECT * FROM demo.test WHERE id = ?";
    private static final String DELETE_DATA_SQL = "DELETE FROM demo.test WHERE id = ?";
    private static final String DROP_TABLE_SQL = "DROP TABLE IF EXISTS demo.test";
    private static final String DROP_KEYSPACE = "DROP KEYSPACE IF EXISTS demo";
    private Cluster cluster;
    private Session session;
    private Logger logger = LogManager.getLogger(CaseController.class);
    @Value("${cassandra.host}")
    private String host;
    @Value("${cassandra.port}")
    private int port;

    @GetMapping("/cassandra")
    public String cassandraCase() {
        logger.info("cassandra contact points: {}:{}", host, port);

        try {
            cluster = Cluster.builder().addContactPoint(host).withPort(port).build();
            session = cluster.connect();

            session.execute(CREATE_KEYSPACE_SQL);
            session.execute(CREATE_TABLE_SQL);

            PreparedStatement insertDataPreparedStatement = session.prepare(INSERT_DATA_SQL);
            ResultSet insertDataResultSet = session.execute(insertDataPreparedStatement.bind("101", "foobar"));
            logger.info(insertDataResultSet.toString());

            PreparedStatement selectDataPreparedStatement = session.prepare(SELECT_DATA_SQL);
            ResultSet resultSet = session.execute(selectDataPreparedStatement.bind("101"));
            Row row = resultSet.one();
            logger.info("id: {}, value: {}", row.getString("id"), row.getString("value"));

            PreparedStatement deleteDataPreparedStatement = session.prepare(DELETE_DATA_SQL);
            ResultSet deleteDataResultSet = session.execute(deleteDataPreparedStatement.bind("101"));
            logger.info(deleteDataResultSet.toString());

            session.execute(DROP_TABLE_SQL);
            session.execute(DROP_KEYSPACE);
        } finally {
            session.close();
            cluster.close();
        }

        return "ok";
    }
}
