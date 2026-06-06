import 'package:get_it/get_it.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../../presentation/bloc/selection/selection_bloc.dart';
import '../network/api_client.dart';
import '../network/local/app_database.dart';
import '../../data/datasources/reader_local_datasource.dart';
import '../../data/datasources/reader_remote_datasource.dart';
import '../../data/repositories/reader_repository_impl.dart';
import '../../domain/repositories/reader_repository.dart';
import '../../domain/usecases/get_chapter_verses_usecase.dart';
import '../../domain/usecases/get_next_chapter_usecase.dart';
import '../../presentation/bloc/reader/reader_bloc.dart';
import '../../data/datasources/selection_local_datasource.dart';
import '../../data/datasources/selection_remote_datasource.dart';
import '../../data/repositories/selection_repository_impl.dart';
import '../../domain/repositories/selection_repository.dart';
import '../../domain/usecases/download_bible_usecase.dart';
import '../../domain/usecases/get_available_bibles_usecase.dart';
import '../../presentation/bloc/settings/settings_bloc.dart';

final sl = GetIt.instance;

Future<void> initDependencies() async {
  // External
  final db = await AppDatabase.create();
  final prefs = await SharedPreferences.getInstance();

  sl.registerSingleton<AppDatabase>(db);
  sl.registerSingleton<SharedPreferences>(prefs);
  sl.registerSingleton<ApiClient>(ApiClient());

  // DAOs
  sl.registerSingleton(db.bibleDao);
  sl.registerSingleton(db.verseDao);
  sl.registerSingleton(db.chapterDao);

  // DataSources
  sl.registerLazySingleton<SelectionRemoteDataSource>(
    () => SelectionRemoteDataSourceImpl(sl()),
  );
  sl.registerLazySingleton<SelectionLocalDataSource>(
    () => SelectionLocalDataSourceImpl(sl(), sl()),
  );
  sl.registerLazySingleton<ReaderRemoteDataSource>(
    () => ReaderRemoteDataSourceImpl(sl()),
  );
  sl.registerLazySingleton<ReaderLocalDataSource>(
    () => ReaderLocalDataSourceImpl(sl(), sl()),
  );

  // Repositories
  sl.registerLazySingleton<SelectionRepository>(
    () => SelectionRepositoryImpl(sl(), sl()),
  );
  sl.registerLazySingleton<ReaderRepository>(
    () => ReaderRepositoryImpl(sl(), sl()),
  );

  // Use Cases
  sl.registerLazySingleton(() => GetAvailableBiblesUseCase(sl()));
  sl.registerLazySingleton(() => DownloadBibleUseCase(sl()));
  sl.registerLazySingleton(() => GetChapterVersesUseCase(sl()));
  sl.registerLazySingleton(() => GetNextChapterUseCase(sl()));

  // BLoCs
  sl.registerFactory(
    () => SelectionBloc(
      getAvailableBibles: sl(),
      downloadBible: sl(),
      prefs: sl(),
    ),
  );
  sl.registerFactory(
    () => ReaderBloc(
      getChapterVerses: sl(),
      getNextChapter: sl(),
      prefs: sl(),
    ),
  );
  sl.registerFactory(() => SettingsBloc(prefs: sl()));
}
