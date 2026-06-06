// lib/features/reader/domain/entities/chapter_entity.dart

import 'package:equatable/equatable.dart';

class ChapterEntity extends Equatable {
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

  bool get hasNext => nextId != null && nextId!.isNotEmpty;
  bool get hasPrevious => previousId != null && previousId!.isNotEmpty;

  @override
  List<Object?> get props => [id, bibleId, bookId, number];
}
