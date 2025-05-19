package oop.tegevusteplaneerija.common.util;

public class SQLStatements {

   // language=SQLite
   public static final String CREATE_EVENTS_TABLE = """
             CREATE TABLE IF NOT EXISTS events (
                 id INTEGER PRIMARY KEY AUTOINCREMENT,
                 nimi TEXT NOT NULL,
                 kirjeldus TEXT,
                 algushetk TIMESTAMP NOT NULL,
                 lopphetk TIMESTAMP NOT NULL,
                 grupp INTEGER NOT NULL
             )
         """;
   // language=SQLite
   public static final String CREATE_GRUPID_TABLE = """
             CREATE TABLE IF NOT EXISTS grupid (
         	id INTEGER PRIMARY KEY AUTOINCREMENT,
         	nimi TEXT NOT NULL,
         	omanik INTEGER NOT NULL,
         	personal INTEGER,
         	FOREIGN KEY(omanik) REFERENCES kasutajad(id)
         );
         """;
   // language=SQLite
   public static final String CREATE_GRUPILIIKMED_TABLE = """
         CREATE TABLE IF NOT EXISTS grupiliikmed (
         	grupp INTEGER NOT NULL,
         	liige INTEGER NOT NULL,
         FOREIGN KEY(grupp) REFERENCES grupid(id),
         FOREIGN KEY(liige) REFERENCES kasutajad(id)
         );
         """;
   // language=SQLite
   public static final String CREATE_KASUTAJAD_TABLE = """
         CREATE TABLE IF NOT EXISTS kasutajad (
         	id integer PRIMARY KEY AUTOINCREMENT,
         	nimi TEXT NOT NULL
         );
         """;
   // language=SQLite
   public static final String EVENTS_INSERT = """
            INSERT INTO events(nimi, kirjeldus, algushetk, lopphetk, grupp) VALUES(?, ?, ?, ?, ?);
         """;
   // language=SQLite
   public static final String ADD_GROUP = """
            INSERT INTO grupid(nimi, omanik, personal) VALUES(?, ?, ?);
         """;
   // language=SQLite
   public static final String REMOVE_GROUP = """
            DELETE FROM grupid WHERE id = ?;
         """;
   // language=SQLite
   public static final String ADD_EVENT = """
            INSERT INTO events(nimi, kirjeldus, algushetk, lopphetk, grupp) VALUES(?, ?, ?, ?, ?);
         """;
   // language=SQLite
   public static final String REMOVE_EVENT = """
            DELETE FROM events WHERE id = ?;
         """;
   // language=SQLite
   public static final String ADD_GROUP_MEMBER = """
            INSERT INTO grupiliikmed(grupp, liige) VALUES(?, ?);
         """;
   // language=SQLite
   public static final String REMOVE_GROUP_MEMBER = """
            DELETE FROM grupiliikmed WHERE grupp = ? AND liige = ?;
         """;

   public static final String ADD_USER = """
            INSERT INTO Kasutajad(nimi) VALUES(?);
         """;
   // language=SQLite
   public static final String REMOVE_USER = """
            DELETE FROM kasutajad WHERE id = ?;
         """;
   // language=SQLite
   public static final String GET_USER = """
         SELECT id, nimi FROM kasutajad WHERE id = ?
         """;
   // language=SQLite
   public static final String GET_GROUP = """
         SELECT id, nimi, omanik, personal FROM grupid WHERE id = ?
         """;

   // language=SQLite
   public static final String GET_GROUP_MEMBERS = """
         SELECT liige FROM grupiliikmed WHERE grupp = ?
         """;
   // language=SQLite
   public static final String GET_USER_GROUPS = """
         SELECT grupp FROM grupiliikmed WHERE liige = ?
         """;
   // language=SQLite
   public static final String GET_GROUP_EVENTS = """
         SELECT * FROM events WHERE grupp = ?
         """;

   // language=SQLite
   public static final String GET_ALL_EVENTS = """
         SELECT * FROM events
         """;

   // language=SQLite
   public static final String UPDATE_EVENT = """
         UPDATE events SET nimi = ?, kirjeldus = ?, algushetk = ?, lopphetk = ?, grupp = ? WHERE id = ?
         """;

   // language=SQLite
   public static final String GET_USER_EVENTS = """
         SELECT * FROM events WHERE grupp IN (
             SELECT grupp FROM grupiliikmed WHERE liige = ?
         )
         """;

   // language=SQLite
   public static final String GET_EVENT_BY_ID = "SELECT * FROM events WHERE id = ?";

   // language=SQLite
   public static final String GET_PERSONAL_GROUP = "SELECT id, nimi, omanik, personal FROM grupid WHERE omanik = ? AND personal = 1 LIMIT 1";
}
