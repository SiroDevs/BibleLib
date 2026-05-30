// lib/core/network/local/app_database.g.dart
// GENERATED CODE - DO NOT MODIFY BY HAND
// Run: dart run build_runner build --delete-conflicting-outputs

part of 'app_database.dart';

class _$AppDatabaseBuilder {
  _$AppDatabaseBuilder(this.name);

  final String name;

  final List<Migration> _migrations = [];
  Callback? _callback;

  _$AppDatabaseBuilder addMigrations(List<Migration> migrations) {
    _migrations.addAll(migrations);
    return this;
  }

  _$AppDatabaseBuilder addCallback(Callback callback) {
    _callback = callback;
    return this;
  }

  Future<AppDatabase> build() async {
    final path = name;
    final database = _$AppDatabase();
    database.database = await database.open(
      path,
      _migrations,
      _callback,
    );
    return database;
  }
}

class $FloorAppDatabase {
  static _$AppDatabaseBuilder databaseBuilder(String name) =>
      _$AppDatabaseBuilder(name);
}

class _$AppDatabase extends AppDatabase {
  _$AppDatabase([StreamController<String>? listener]) {
    changeListener = listener ?? StreamController<String>.broadcast();
  }

  BibleDao? _bibleDaoInstance;
  VerseDao? _verseDaoInstance;
  ChapterDao? _chapterDaoInstance;

  Future<sqflite.Database> open(
    String path,
    List<Migration> migrations, [
    Callback? callback,
  ]) async {
    final databaseOptions = sqflite.OpenDatabaseOptions(
      version: kDatabaseVersion,
      onConfigure: (database) async {
        await database.execute('PRAGMA foreign_keys = ON');
        await callback?.onConfigure?.call(database);
      },
      onOpen: (database) async {
        await callback?.onOpen?.call(database);
      },
      onUpgrade: (database, startVersion, endVersion) async {
        await MigrationAdapter.runMigrations(
            database, startVersion, endVersion, migrations);
        await callback?.onUpgrade?.call(database, startVersion, endVersion);
      },
      onCreate: (database, version) async {
        await database.execute(
            'CREATE TABLE IF NOT EXISTS `bibles` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `nameLocal` TEXT NOT NULL, `abbreviation` TEXT NOT NULL, `abbreviationLocal` TEXT NOT NULL, `description` TEXT NOT NULL, `language` TEXT NOT NULL, `languageLocal` TEXT NOT NULL, `languageScript` TEXT NOT NULL, `languageScriptDirection` TEXT NOT NULL, `type` TEXT NOT NULL, `updatedAt` TEXT NOT NULL, `isDownloaded` INTEGER NOT NULL, PRIMARY KEY (`id`))');
        await database.execute(
            'CREATE TABLE IF NOT EXISTS `verses` (`id` TEXT NOT NULL, `bibleId` TEXT NOT NULL, `bookId` TEXT NOT NULL, `chapterId` TEXT NOT NULL, `reference` TEXT NOT NULL, `content` TEXT NOT NULL, `verseNumber` INTEGER NOT NULL, PRIMARY KEY (`id`))');
        await database.execute(
            'CREATE INDEX `index_verses_bibleId_chapterId` ON `verses` (`bibleId`, `chapterId`)');
        await database.execute(
            'CREATE INDEX `index_verses_bibleId_bookId` ON `verses` (`bibleId`, `bookId`)');
        await database.execute(
            'CREATE TABLE IF NOT EXISTS `chapters` (`id` TEXT NOT NULL, `bibleId` TEXT NOT NULL, `bookId` TEXT NOT NULL, `number` TEXT NOT NULL, `reference` TEXT, `nextId` TEXT, `previousId` TEXT, PRIMARY KEY (`id`))');
        await database.execute(
            'CREATE INDEX `index_chapters_bibleId_bookId` ON `chapters` (`bibleId`, `bookId`)');
        await callback?.onCreate?.call(database, version);
      },
    );
    return sqfliteDatabaseFactory.openDatabase(path, options: databaseOptions);
  }

  @override
  BibleDao get bibleDao {
    return _bibleDaoInstance ??= _$BibleDao(database, changeListener);
  }

  @override
  VerseDao get verseDao {
    return _verseDaoInstance ??= _$VerseDao(database, changeListener);
  }

  @override
  ChapterDao get chapterDao {
    return _chapterDaoInstance ??= _$ChapterDao(database, changeListener);
  }
}

class _$BibleDao extends BibleDao {
  _$BibleDao(
    this.database,
    this.changeListener,
  )   : _queryAdapter = QueryAdapter(database),
        _bibleModelInsertionAdapter = InsertionAdapter(
            database,
            'bibles',
            (BibleModel item) => <String, Object?>{
                  'id': item.id,
                  'name': item.name,
                  'nameLocal': item.nameLocal,
                  'abbreviation': item.abbreviation,
                  'abbreviationLocal': item.abbreviationLocal,
                  'description': item.description,
                  'language': item.language,
                  'languageLocal': item.languageLocal,
                  'languageScript': item.languageScript,
                  'languageScriptDirection': item.languageScriptDirection,
                  'type': item.type,
                  'updatedAt': item.updatedAt,
                  'isDownloaded': item.isDownloaded
                });

  final sqflite.DatabaseExecutor database;
  final StreamController<String> changeListener;
  final QueryAdapter _queryAdapter;
  final InsertionAdapter<BibleModel> _bibleModelInsertionAdapter;

  @override
  Future<List<BibleModel>> getAllBibles() async {
    return _queryAdapter.queryList('SELECT * FROM bibles',
        mapper: (Map<String, Object?> row) => BibleModel(
            id: row['id'] as String,
            name: row['name'] as String,
            nameLocal: row['nameLocal'] as String,
            abbreviation: row['abbreviation'] as String,
            abbreviationLocal: row['abbreviationLocal'] as String,
            description: row['description'] as String,
            language: row['language'] as String,
            languageLocal: row['languageLocal'] as String,
            languageScript: row['languageScript'] as String,
            languageScriptDirection: row['languageScriptDirection'] as String,
            type: row['type'] as String,
            updatedAt: row['updatedAt'] as String,
            isDownloaded: row['isDownloaded'] as int));
  }

  @override
  Future<List<BibleModel>> getDownloadedBibles() async {
    return _queryAdapter.queryList(
        'SELECT * FROM bibles WHERE isDownloaded = 1',
        mapper: (Map<String, Object?> row) => BibleModel(
            id: row['id'] as String,
            name: row['name'] as String,
            nameLocal: row['nameLocal'] as String,
            abbreviation: row['abbreviation'] as String,
            abbreviationLocal: row['abbreviationLocal'] as String,
            description: row['description'] as String,
            language: row['language'] as String,
            languageLocal: row['languageLocal'] as String,
            languageScript: row['languageScript'] as String,
            languageScriptDirection: row['languageScriptDirection'] as String,
            type: row['type'] as String,
            updatedAt: row['updatedAt'] as String,
            isDownloaded: row['isDownloaded'] as int));
  }

  @override
  Future<BibleModel?> getBibleById(String id) async {
    return _queryAdapter.query('SELECT * FROM bibles WHERE id = ?1',
        mapper: (Map<String, Object?> row) => BibleModel(
            id: row['id'] as String,
            name: row['name'] as String,
            nameLocal: row['nameLocal'] as String,
            abbreviation: row['abbreviation'] as String,
            abbreviationLocal: row['abbreviationLocal'] as String,
            description: row['description'] as String,
            language: row['language'] as String,
            languageLocal: row['languageLocal'] as String,
            languageScript: row['languageScript'] as String,
            languageScriptDirection: row['languageScriptDirection'] as String,
            type: row['type'] as String,
            updatedAt: row['updatedAt'] as String,
            isDownloaded: row['isDownloaded'] as int),
        arguments: [id]);
  }

  @override
  Future<void> insertBible(BibleModel bible) async {
    await _bibleModelInsertionAdapter.insert(
        bible, OnConflictStrategy.replace);
  }

  @override
  Future<void> insertBibles(List<BibleModel> bibles) async {
    await _bibleModelInsertionAdapter.insertList(
        bibles, OnConflictStrategy.replace);
  }

  @override
  Future<void> markAsDownloaded(String id) async {
    await _queryAdapter.queryNoReturn(
        'UPDATE bibles SET isDownloaded = 1 WHERE id = ?1',
        arguments: [id]);
  }

  @override
  Future<void> deleteBible(String id) async {
    await _queryAdapter.queryNoReturn(
        'DELETE FROM bibles WHERE id = ?1',
        arguments: [id]);
  }
}

class _$VerseDao extends VerseDao {
  _$VerseDao(
    this.database,
    this.changeListener,
  )   : _queryAdapter = QueryAdapter(database),
        _verseModelInsertionAdapter = InsertionAdapter(
            database,
            'verses',
            (VerseModel item) => <String, Object?>{
                  'id': item.id,
                  'bibleId': item.bibleId,
                  'bookId': item.bookId,
                  'chapterId': item.chapterId,
                  'reference': item.reference,
                  'content': item.content,
                  'verseNumber': item.verseNumber
                });

  final sqflite.DatabaseExecutor database;
  final StreamController<String> changeListener;
  final QueryAdapter _queryAdapter;
  final InsertionAdapter<VerseModel> _verseModelInsertionAdapter;

  @override
  Future<List<VerseModel>> getVersesByChapter(
      String bibleId, String chapterId) async {
    return _queryAdapter.queryList(
        'SELECT * FROM verses WHERE bibleId = ?1 AND chapterId = ?2 ORDER BY verseNumber',
        mapper: (Map<String, Object?> row) => VerseModel(
            id: row['id'] as String,
            bibleId: row['bibleId'] as String,
            bookId: row['bookId'] as String,
            chapterId: row['chapterId'] as String,
            reference: row['reference'] as String,
            content: row['content'] as String,
            verseNumber: row['verseNumber'] as int),
        arguments: [bibleId, chapterId]);
  }

  @override
  Future<int?> countVersesInChapter(String bibleId, String chapterId) async {
    return _queryAdapter.query(
        'SELECT COUNT(*) FROM verses WHERE bibleId = ?1 AND chapterId = ?2',
        mapper: (Map<String, Object?> row) => row.values.first as int,
        arguments: [bibleId, chapterId]);
  }

  @override
  Future<void> insertVerses(List<VerseModel> verses) async {
    await _verseModelInsertionAdapter.insertList(
        verses, OnConflictStrategy.replace);
  }

  @override
  Future<void> deleteVersesByBible(String bibleId) async {
    await _queryAdapter.queryNoReturn(
        'DELETE FROM verses WHERE bibleId = ?1',
        arguments: [bibleId]);
  }

  @override
  Future<List<String>> getDownloadedChapterIds(String bibleId) async {
    return _queryAdapter.queryList(
        'SELECT DISTINCT chapterId FROM verses WHERE bibleId = ?1',
        mapper: (Map<String, Object?> row) => row.values.first as String,
        arguments: [bibleId]);
  }
}

class _$ChapterDao extends ChapterDao {
  _$ChapterDao(
    this.database,
    this.changeListener,
  )   : _queryAdapter = QueryAdapter(database),
        _chapterModelInsertionAdapter = InsertionAdapter(
            database,
            'chapters',
            (ChapterModel item) => <String, Object?>{
                  'id': item.id,
                  'bibleId': item.bibleId,
                  'bookId': item.bookId,
                  'number': item.number,
                  'reference': item.reference,
                  'nextId': item.nextId,
                  'previousId': item.previousId
                });

  final sqflite.DatabaseExecutor database;
  final StreamController<String> changeListener;
  final QueryAdapter _queryAdapter;
  final InsertionAdapter<ChapterModel> _chapterModelInsertionAdapter;

  @override
  Future<List<ChapterModel>> getChaptersByBook(
      String bibleId, String bookId) async {
    return _queryAdapter.queryList(
        'SELECT * FROM chapters WHERE bibleId = ?1 AND bookId = ?2 ORDER BY CAST(number AS INTEGER)',
        mapper: (Map<String, Object?> row) => ChapterModel(
            id: row['id'] as String,
            bibleId: row['bibleId'] as String,
            bookId: row['bookId'] as String,
            number: row['number'] as String,
            reference: row['reference'] as String?,
            nextId: row['nextId'] as String?,
            previousId: row['previousId'] as String?),
        arguments: [bibleId, bookId]);
  }

  @override
  Future<ChapterModel?> getChapter(String chapterId, String bibleId) async {
    return _queryAdapter.query(
        'SELECT * FROM chapters WHERE id = ?1 AND bibleId = ?2',
        mapper: (Map<String, Object?> row) => ChapterModel(
            id: row['id'] as String,
            bibleId: row['bibleId'] as String,
            bookId: row['bookId'] as String,
            number: row['number'] as String,
            reference: row['reference'] as String?,
            nextId: row['nextId'] as String?,
            previousId: row['previousId'] as String?),
        arguments: [chapterId, bibleId]);
  }

  @override
  Future<void> insertChapters(List<ChapterModel> chapters) async {
    await _chapterModelInsertionAdapter.insertList(
        chapters, OnConflictStrategy.replace);
  }

  @override
  Future<void> deleteChaptersByBible(String bibleId) async {
    await _queryAdapter.queryNoReturn(
        'DELETE FROM chapters WHERE bibleId = ?1',
        arguments: [bibleId]);
  }
}
