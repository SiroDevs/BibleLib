import 'package:equatable/equatable.dart';

class VerseModel extends Equatable {
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

  @override
  List<Object?> get props => [id, bibleId, chapterId];
}
