package com.example.reactionchallenge.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.example.reactionchallenge.data.local.entity.GameSessionEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class GameSessionDao_Impl implements GameSessionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<GameSessionEntity> __insertionAdapterOfGameSessionEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllForPlayer;

  public GameSessionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfGameSessionEntity = new EntityInsertionAdapter<GameSessionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `game_sessions` (`id`,`playerName`,`difficulty`,`totalScore`,`levelsCompleted`,`totalLevels`,`totalCorrect`,`totalAttempts`,`avgReactionTimeMs`,`won`,`timestamp`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final GameSessionEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getPlayerName());
        statement.bindString(3, entity.getDifficulty());
        statement.bindLong(4, entity.getTotalScore());
        statement.bindLong(5, entity.getLevelsCompleted());
        statement.bindLong(6, entity.getTotalLevels());
        statement.bindLong(7, entity.getTotalCorrect());
        statement.bindLong(8, entity.getTotalAttempts());
        statement.bindLong(9, entity.getAvgReactionTimeMs());
        final int _tmp = entity.getWon() ? 1 : 0;
        statement.bindLong(10, _tmp);
        statement.bindLong(11, entity.getTimestamp());
      }
    };
    this.__preparedStmtOfDeleteAllForPlayer = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM game_sessions WHERE playerName = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final GameSessionEntity session,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfGameSessionEntity.insertAndReturnId(session);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAllForPlayer(final String playerName,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllForPlayer.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, playerName);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteAllForPlayer.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<GameSessionEntity>> getSessionsByPlayer(final String playerName) {
    final String _sql = "\n"
            + "        SELECT * FROM game_sessions\n"
            + "        WHERE playerName = ?\n"
            + "        ORDER BY totalScore DESC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, playerName);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"game_sessions"}, new Callable<List<GameSessionEntity>>() {
      @Override
      @NonNull
      public List<GameSessionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPlayerName = CursorUtil.getColumnIndexOrThrow(_cursor, "playerName");
          final int _cursorIndexOfDifficulty = CursorUtil.getColumnIndexOrThrow(_cursor, "difficulty");
          final int _cursorIndexOfTotalScore = CursorUtil.getColumnIndexOrThrow(_cursor, "totalScore");
          final int _cursorIndexOfLevelsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "levelsCompleted");
          final int _cursorIndexOfTotalLevels = CursorUtil.getColumnIndexOrThrow(_cursor, "totalLevels");
          final int _cursorIndexOfTotalCorrect = CursorUtil.getColumnIndexOrThrow(_cursor, "totalCorrect");
          final int _cursorIndexOfTotalAttempts = CursorUtil.getColumnIndexOrThrow(_cursor, "totalAttempts");
          final int _cursorIndexOfAvgReactionTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "avgReactionTimeMs");
          final int _cursorIndexOfWon = CursorUtil.getColumnIndexOrThrow(_cursor, "won");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final List<GameSessionEntity> _result = new ArrayList<GameSessionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final GameSessionEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpPlayerName;
            _tmpPlayerName = _cursor.getString(_cursorIndexOfPlayerName);
            final String _tmpDifficulty;
            _tmpDifficulty = _cursor.getString(_cursorIndexOfDifficulty);
            final int _tmpTotalScore;
            _tmpTotalScore = _cursor.getInt(_cursorIndexOfTotalScore);
            final int _tmpLevelsCompleted;
            _tmpLevelsCompleted = _cursor.getInt(_cursorIndexOfLevelsCompleted);
            final int _tmpTotalLevels;
            _tmpTotalLevels = _cursor.getInt(_cursorIndexOfTotalLevels);
            final int _tmpTotalCorrect;
            _tmpTotalCorrect = _cursor.getInt(_cursorIndexOfTotalCorrect);
            final int _tmpTotalAttempts;
            _tmpTotalAttempts = _cursor.getInt(_cursorIndexOfTotalAttempts);
            final long _tmpAvgReactionTimeMs;
            _tmpAvgReactionTimeMs = _cursor.getLong(_cursorIndexOfAvgReactionTimeMs);
            final boolean _tmpWon;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfWon);
            _tmpWon = _tmp != 0;
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            _item = new GameSessionEntity(_tmpId,_tmpPlayerName,_tmpDifficulty,_tmpTotalScore,_tmpLevelsCompleted,_tmpTotalLevels,_tmpTotalCorrect,_tmpTotalAttempts,_tmpAvgReactionTimeMs,_tmpWon,_tmpTimestamp);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<GameSessionEntity>> getTopSessions(final int limit) {
    final String _sql = "SELECT * FROM game_sessions ORDER BY totalScore DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"game_sessions"}, new Callable<List<GameSessionEntity>>() {
      @Override
      @NonNull
      public List<GameSessionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPlayerName = CursorUtil.getColumnIndexOrThrow(_cursor, "playerName");
          final int _cursorIndexOfDifficulty = CursorUtil.getColumnIndexOrThrow(_cursor, "difficulty");
          final int _cursorIndexOfTotalScore = CursorUtil.getColumnIndexOrThrow(_cursor, "totalScore");
          final int _cursorIndexOfLevelsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "levelsCompleted");
          final int _cursorIndexOfTotalLevels = CursorUtil.getColumnIndexOrThrow(_cursor, "totalLevels");
          final int _cursorIndexOfTotalCorrect = CursorUtil.getColumnIndexOrThrow(_cursor, "totalCorrect");
          final int _cursorIndexOfTotalAttempts = CursorUtil.getColumnIndexOrThrow(_cursor, "totalAttempts");
          final int _cursorIndexOfAvgReactionTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "avgReactionTimeMs");
          final int _cursorIndexOfWon = CursorUtil.getColumnIndexOrThrow(_cursor, "won");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final List<GameSessionEntity> _result = new ArrayList<GameSessionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final GameSessionEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpPlayerName;
            _tmpPlayerName = _cursor.getString(_cursorIndexOfPlayerName);
            final String _tmpDifficulty;
            _tmpDifficulty = _cursor.getString(_cursorIndexOfDifficulty);
            final int _tmpTotalScore;
            _tmpTotalScore = _cursor.getInt(_cursorIndexOfTotalScore);
            final int _tmpLevelsCompleted;
            _tmpLevelsCompleted = _cursor.getInt(_cursorIndexOfLevelsCompleted);
            final int _tmpTotalLevels;
            _tmpTotalLevels = _cursor.getInt(_cursorIndexOfTotalLevels);
            final int _tmpTotalCorrect;
            _tmpTotalCorrect = _cursor.getInt(_cursorIndexOfTotalCorrect);
            final int _tmpTotalAttempts;
            _tmpTotalAttempts = _cursor.getInt(_cursorIndexOfTotalAttempts);
            final long _tmpAvgReactionTimeMs;
            _tmpAvgReactionTimeMs = _cursor.getLong(_cursorIndexOfAvgReactionTimeMs);
            final boolean _tmpWon;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfWon);
            _tmpWon = _tmp != 0;
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            _item = new GameSessionEntity(_tmpId,_tmpPlayerName,_tmpDifficulty,_tmpTotalScore,_tmpLevelsCompleted,_tmpTotalLevels,_tmpTotalCorrect,_tmpTotalAttempts,_tmpAvgReactionTimeMs,_tmpWon,_tmpTimestamp);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getBestScore(final String playerName, final String difficulty,
      final Continuation<? super Integer> $completion) {
    final String _sql = "\n"
            + "        SELECT MAX(totalScore) FROM game_sessions\n"
            + "        WHERE playerName = ? AND difficulty = ?\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, playerName);
    _argIndex = 2;
    _statement.bindString(_argIndex, difficulty);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @Nullable
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final Integer _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
