// lib/features/reader/data/models/chapter_model.dart

import 'package:biblelib/features/reader/domain/entities/chapter_entity.dart';
import 'package:floor/floor.dart';

@Entity(
  tableName: 'chapters',
  indices: [Index(value: ['bibleId', 'bookId'])],
)
class ChapterModel {
  @PrimaryKey()
  final String id;
  final String bibleId;
  final String bookId;
  final String number;
  final String? reference;
  final String? nextId;
  final String? previousId;

  const ChapterModel({
    required this.id,
    required this.bibleId,
    required this.bookId,
    required this.number,
    this.reference,
    this.nextId,
    this.previousId,
  });

  factory ChapterModel.fromJson(
    Map<String, dynamic> json, {
    required String bibleId,
  }) {
    final next = json['next'] as Map<String, dynamic>?;
    final previous = json['previous'] as Map<String, dynamic>?;
    return ChapterModel(
      id: json['id'] as String? ?? '',
      bibleId: bibleId,
      bookId: json['bookId'] as String? ?? '',
      number: json['number'] as String? ?? '',
      reference: json['reference'] as String?,
      nextId: next?['id'] as String?,
      previousId: previous?['id'] as String?,
    );
  }

  ChapterEntity toEntity() => ChapterEntity(
        id: id,
        bibleId: bibleId,
        bookId: bookId,
        number: number,
        reference: reference,
        nextId: nextId,
        previousId: previousId,
      );
}
