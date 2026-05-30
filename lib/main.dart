import 'package:biblelib/app.dart';
import 'package:biblelib/core/di/service_locator.dart';
import 'package:biblelib/core/utils/bible_download_worker.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_dotenv/flutter_dotenv.dart';

@pragma('vm:entry-point')
void callbackDispatcher() => bibleLibCallbackDispatcher();

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();

  await dotenv.load(fileName: '.env');

  await SystemChrome.setPreferredOrientations([
    DeviceOrientation.portraitUp,
    DeviceOrientation.portraitDown,
  ]);

  SystemChrome.setSystemUIOverlayStyle(
    const SystemUiOverlayStyle(
      statusBarColor: Colors.transparent,
      statusBarIconBrightness: Brightness.dark,
    ),
  );

  await BibleDownloadWorker.initialize();
  await initDependencies();

  runApp(const BibleLibApp());
}
