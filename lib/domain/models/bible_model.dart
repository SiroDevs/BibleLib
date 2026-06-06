import 'package:equatable/equatable.dart';

class BibleModel extends Equatable {
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
  final bool isSelected;

  const BibleModel({
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
    this.isSelected = false,
  });

  BibleModel copyWith({bool? isDownloaded, bool? isSelected}) => BibleModel(
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
        isSelected: isSelected ?? this.isSelected,
      );

  @override
  List<Object?> get props => [id, name, abbreviation, isDownloaded, isSelected];
}
