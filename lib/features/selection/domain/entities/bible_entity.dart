// lib/features/selection/domain/entities/bible_entity.dart

import 'package:equatable/equatable.dart';

class BibleEntity extends Equatable {
  final String id;
  final String name;
  final String nameLocal;
  final String abbreviation;
  final String abbreviationLocal;
  final String description;
  final String language;
  final String languageLocal;
  final String languageScript;
  final String languageScriptDirection;
  final String type;
  final String updatedAt;
  final bool isDownloaded;

  const BibleEntity({
    required this.id,
    required this.name,
    required this.nameLocal,
    required this.abbreviation,
    required this.abbreviationLocal,
    required this.description,
    required this.language,
    required this.languageLocal,
    required this.languageScript,
    required this.languageScriptDirection,
    required this.type,
    required this.updatedAt,
    this.isDownloaded = false,
  });

  BibleEntity copyWith({bool? isDownloaded}) => BibleEntity(
        id: id,
        name: name,
        nameLocal: nameLocal,
        abbreviation: abbreviation,
        abbreviationLocal: abbreviationLocal,
        description: description,
        language: language,
        languageLocal: languageLocal,
        languageScript: languageScript,
        languageScriptDirection: languageScriptDirection,
        type: type,
        updatedAt: updatedAt,
        isDownloaded: isDownloaded ?? this.isDownloaded,
      );

  @override
  List<Object?> get props => [
        id, name, abbreviation, isDownloaded,
      ];
}
