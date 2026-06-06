import 'package:froom/froom.dart';

import '../../../domain/models/chapter_model.dart';

@Entity(
  tableName: 'chapters',
  indices: [Index(value: ['bibleId', 'bookId'])],
)
class ChapterEntity {
  @PrimaryKey()
  final String id;
  final String bibleId;
  final String bookId;
  final String number;
  final String? reference;
  final String? nextId;
  final String? previousId;

  const ChapterEntity({
    required this.id,
    required this.bibleId,
    required this.bookId,
    required this.number,
    this.reference,
    this.nextId,
    this.previousId,
  });

  factory ChapterEntity.fromJson(
    Map<String, dynamic> json, {
    required String bibleId,
  }) {
    final next = json['next'] as Map<String, dynamic>?;
    final previous = json['previous'] as Map<String, dynamic>?;
    return ChapterEntity(
      id: json['id'] as String? ?? '',
      bibleId: bibleId,
      bookId: json['bookId'] as String? ?? '',
      number: json['number'] as String? ?? '',
      reference: json['reference'] as String?,
      nextId: next?['id'] as String?,
      previousId: previous?['id'] as String?,
    );
  }

  ChapterModel toModel() => ChapterModel(
        id: id,
        bibleId: bibleId,
        bookId: bookId,
        number: number,
        reference: reference,
        nextId: nextId,
        previousId: previousId,
      );
}
