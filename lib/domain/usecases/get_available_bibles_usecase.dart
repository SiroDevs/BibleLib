import 'package:dartz/dartz.dart';

import '../../core/errors/failures.dart';
import '../models/bible_model.dart';
import '../repos/selection_repo.dart';

class GetAvailableBiblesUseCase {
  final SelectionRepo _repository;
  const GetAvailableBiblesUseCase(this._repository);

  Future<Either<Failure, List<BibleModel>>> call() =>
      _repository.getAvailableBibles();
}
