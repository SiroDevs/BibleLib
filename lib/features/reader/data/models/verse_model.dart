// lib/features/reader/data/models/verse_model.dart

import 'package:biblelib/features/reader/domain/entities/verse_entity.dart';
import 'package:floor/floor.dart';

@Entity(
  tableName: 'verses',
  indices: [
    Index(value: ['bibleId', 'chapterId']),
    Index(value: ['bibleId', 'bookId']),
  ],
)
class VerseModel {
  @PrimaryKey()
  final String id;
  final String bibleId;
  final String bookId;
  final String chapterId;
  final String reference;
  final String content;
  final int verseNumber;

  const VerseModel({
    required this.id,
    required this.bibleId,
    required this.bookId,
    required this.chapterId,
    required this.reference,
    required this.content,
    required this.verseNumber,
  });

  factory VerseModel.fromJson(
    Map<String, dynamic> json, {
    required String bibleId,
    required String bookId,
    required String chapterId,
  }) {
    final id = json['id'] as String? ?? '';
    final numStr = id.split('.').lastOrNull ?? '0';
    return VerseModel(
      id: id,
      bibleId: bibleId,
      bookId: bookId,
      chapterId: chapterId,
      reference: json['reference'] as String? ?? '',
      content: json['content'] as String? ?? '',
      verseNumber: int.tryParse(numStr) ?? 0,
    );
  }

  VerseEntity toEntity() => VerseEntity(
        id: id,
        bibleId: bibleId,
        bookId: bookId,
        chapterId: chapterId,
        reference: reference,
        content: content,
        verseNumber: verseNumber,
      );
}
