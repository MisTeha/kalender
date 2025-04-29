package oop.tegevusteplaneerija.common.util;

public class SQLStatements {

    public static final String CREATE_EVENTS_TABLE = """
                CREATE TABLE IF NOT EXISTS events (
                    ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    NIMI TEXT NOT NULL,
                    KIRJELDUS TEXT,
                    ALGUSHETK TIMESTAMP NOT NULL,
                    LOPPHETK TIMESTAMP NOT NULL,
                    GRUPP INTEGER NOT NULL
                )
            """;

    public static final String CREATE_GRUPID_TABLE = """
                CREATE TABLE IF NOT EXISTS Grupid (
            	id INTEGER PRIMARY KEY AUTOINCREMENT,
            	nimi TEXT NOT NULL,
            	omanik INTEGER NOT NULL,
            	FOREIGN KEY(omanik) REFERENCES Kasutajad(id)
            );
            """;

    public static final String CREATE_GRUPILIIKMED_TABLE = """
            CREATE TABLE IF NOT EXISTS GrupiLiikmed (
            	grupp INTEGER NOT NULL,
            	liige INTEGER NOT NULL,
            FOREIGN KEY(grupp) REFERENCES Grupid(id),
            FOREIGN KEY(liige) REFERENCES Kasutajad(id)
            );
            """;

    public static final String CREATE_KASUTAJAD_TABLE = """
            CREATE TABLE IF NOT EXISTS kasutajad (
            	id integer PRIMARY KEY AUTOINCREMENT,
            	nimi TEXT NOT NULL,
            	pgrupp INTEGER NOT NULL,
            FOREIGN KEY(pgrupp) REFERENCES grupid(id)
            );
            """;

    public static final String EVENTS_INSERT = """
               INSERT INTO events(NIMI, KIRJELDUS, ALGUSHETK, LOPPHETK, GRUPP) VALUES(?, ?, ?, ?, ?);
            """;

    public static final String ADD_GROUP = """
               INSERT INTO grupid(nimi, omanik) VALUES(?, ?);
            """;

    public static final String REMOVE_GROUP = """
               DELETE FROM Grupid WHERE id = ?;
            """;

    public static final String ADD_EVENT = """
               INSERT INTO events(NIMI, KIRJELDUS, ALGUSHETK, LOPPHETK, GRUPP) VALUES(?, ?, ?, ?, ?);
            """;

    public static final String REMOVE_EVENT = """
               DELETE FROM events WHERE ID = ?;
            """;

    public static final String ADD_GROUP_MEMBER = """
               INSERT INTO GrupiLiikmed(grupp, liige) VALUES(?, ?);
            """;

    public static final String REMOVE_GROUP_MEMBER = """
               DELETE FROM GrupiLiikmed WHERE grupp = ? AND liige = ?;
            """;

    public static final String ADD_USER = """
               INSERT INTO Kasutajad(nimi, pgrupp) VALUES(?, ?);
            """;

    public static final String REMOVE_USER = """
               DELETE FROM Kasutajad WHERE id = ?;
            """;

}
