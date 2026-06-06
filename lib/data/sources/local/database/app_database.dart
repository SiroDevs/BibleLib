import 'dart:async';

import 'package:froom/froom.dart';
import 'package:sqflite/sqflite.dart' as sqflite;

import '../../../../core/constants/app_constants.dart';
import '../../../entities/bible_entity.dart';
import '../../../entities/chapter_entity.dart';
import '../../../entities/verse_entity.dart';
import '../daos/bible_dao.dart';
import '../daos/chapter_dao.dart';
import '../daos/verse_dao.dart';

part 'app_database.g.dart';

@Database(
  version: kDatabaseVersion,
  entities: [BibleEntity, VerseEntity, ChapterEntity],
)
abstract class AppDatabase extends FroomDatabase {
  BibleDao get bibleDao;
  VerseDao get verseDao;
  ChapterDao get chapterDao;

  static Future<AppDatabase> create() async {
    return await $FroomAppDatabase
        .databaseBuilder(kDatabaseName)
        .addMigrations([])
        .build();
  }
}
