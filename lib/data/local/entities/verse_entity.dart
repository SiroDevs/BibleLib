import 'package:froom/froom.dart';

import '../../../domain/models/verse_model.dart';

@Entity(
  tableName: 'verses',
  indices: [
    Index(value: ['bibleId', 'chapterId']),
    Index(value: ['bibleId', 'bookId']),
  ],
)
class VerseEntity {
  @PrimaryKey()
  final String id;
  final String bibleId;
  final String bookId;
  final String chapterId;
  final String reference;
  final String content;
  final int verseNumber;

  const VerseEntity({
    required this.id,
    required this.bibleId,
    required this.bookId,
    required this.chapterId,
    required this.reference,
    required this.content,
    required this.verseNumber,
  });

  factory VerseEntity.fromJson(
    Map<String, dynamic> json, {
    required String bibleId,
    required String bookId,
    required String chapterId,
  }) {
    final id = json['id'] as String? ?? '';
    final numStr = id.split('.').lastOrNull ?? '0';
    return VerseEntity(
      id: id,
      bibleId: bibleId,
      bookId: bookId,
      chapterId: chapterId,
      reference: json['reference'] as String? ?? '',
      content: json['content'] as String? ?? '',
      verseNumber: int.tryParse(numStr) ?? 0,
    );
  }

  VerseModel toModel() => VerseModel(
        id: id,
        bibleId: bibleId,
        bookId: bookId,
        chapterId: chapterId,
        reference: reference,
        content: content,
        verseNumber: verseNumber,
      );
}
