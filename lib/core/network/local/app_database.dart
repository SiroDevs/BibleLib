// lib/core/network/local/app_database.dart

// dart run build_runner build --delete-conflicting-outputs

import 'dart:async';

import 'package:biblelib/core/constants/app_constants.dart';
import 'package:biblelib/core/network/local/daos/bible_dao.dart';
import 'package:biblelib/core/network/local/daos/chapter_dao.dart';
import 'package:biblelib/core/network/local/daos/verse_dao.dart';
import 'package:biblelib/features/reader/data/models/chapter_model.dart';
import 'package:biblelib/features/reader/data/models/verse_model.dart';
import 'package:biblelib/features/selection/data/models/bible_model.dart';
import 'package:floor/floor.dart';
import 'package:sqflite/sqflite.dart' as sqflite;

part 'app_database.g.dart';

@Database(
  version: kDatabaseVersion,
  entities: [BibleModel, VerseModel, ChapterModel],
)
abstract class AppDatabase extends FloorDatabase {
  BibleDao get bibleDao;
  VerseDao get verseDao;
  ChapterDao get chapterDao;

  static Future<AppDatabase> create() async {
    return await $FloorAppDatabase
        .databaseBuilder(kDatabaseName)
        .addMigrations([])
        .build();
  }
}
