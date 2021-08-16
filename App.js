/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow strict-local
 */

import React, {useEffect, useState, useCallback, useRef} from 'react';
import LinearGradient from 'react-native-linear-gradient';

import {
  SafeAreaView,
  TouchableOpacity,
  Text,
  StatusBar,
  StyleSheet,
  useColorScheme,
  NativeModules,
  NativeEventEmitter,
  Alert,
  Image,
  ScrollView,
  Dimensions,
} from 'react-native';
import ActionSheet from 'react-native-actions-sheet';

const Color = ({name, color}) => (
  <TouchableOpacity
    onPress={() => NativeModules.CustomModule.copyToClipboard(name, color)}
    style={{
      borderRadius: 9,
      backgroundColor: color,
      width: '100%',
      height: 56,
      marginVertical: 4,
      justifyContent: 'center',
    }}
    activeOpacity={1}>
    <LinearGradient
      start={{x: 0, y: 0}}
      end={{x: 1, y: 0}}
      colors={[
        '#000000fa',
        '#000000c2',
        '#000000a5',
        '#00000066',
        '#00000011',
        '#00000000',
      ]}
      style={{height: '100%', width: '45%', padding: 10}}>
      <Text style={{fontSize: 18, fontWeight: '900', color: 'white'}}>
        {color}
      </Text>
    </LinearGradient>
  </TouchableOpacity>
);

const App = () => {
  const actionSheetRef = useRef();
  const [colors, setColors] = useState(null);
  const [source, setSource] = useState(null);
  const isDarkMode = useColorScheme() === 'dark';

  const _setSource = useCallback(uri => setSource(uri), []);
  const _clearColors = useCallback(() => setColors(null), []);

  useEffect(() => {
    const eventEmitter = new NativeEventEmitter(NativeModules.CustomModule);

    eventEmitter.addListener('BUILD_VERSION', version =>
      Alert.alert(`version is ${version}`),
    );

    eventEmitter.addListener('IMAGE_SOURCE', uri => {
      _setSource('file://' + uri);
    });

    eventEmitter.addListener('COLORS_LIST', list => {
      setColors(list);
      actionSheetRef?.current?.setModalVisible();
    });

    return () => {
      eventEmitter.removeListener();
    };
  }, [_setSource, _clearColors]);

  useEffect(() => {
    if (source) {
      _clearColors();
    }
  }, [source, _clearColors]);

  const backgroundStyle = {
    backgroundColor: '#000000',
    flex: 1,
    alignItems: 'center',
    justifyContent: 'flex-end',
    padding: 40,
  };

  return (
    <SafeAreaView style={backgroundStyle}>
      <StatusBar barStyle="light-content" />
      {source && (
        <Image
          style={{width: Dimensions.get('window').width, height: 500}}
          resizeMode="contain"
          onError={e => console.log('image ', e.nativeEvent.error)}
          source={{uri: source}}
        />
      )}
      {source && (
        <TouchableOpacity
          onPress={() => {
            if (!colors) {
              NativeModules.CustomModule.getLastColorsList();
            } else {
              actionSheetRef?.current?.setModalVisible();
            }
          }}
          activeOpacity={0.8}
          style={{
            marginTop: 40,
            backgroundColor: '#3ddc84',
            paddingHorizontal: 20,
            paddingVertical: 16,
            borderRadius: 40,
            height: 54,
            width: '100%',
            alignItems: 'center',
          }}>
          <Text style={{fontWeight: '900', fontSize: 18}}>
            {!colors ? 'Extract Colors' : 'Show Colors'}
          </Text>
        </TouchableOpacity>
      )}
      <TouchableOpacity
        onPress={() => NativeModules.CustomModule.navigateToNativeActivity()}
        activeOpacity={0.8}
        style={{
          marginTop: 40,
          backgroundColor: '#3ddc84',
          paddingHorizontal: 20,
          paddingVertical: 16,
          borderRadius: 40,
          height: 54,
          width: '100%',
          alignItems: 'center',
        }}>
        <Text style={{fontWeight: '900', fontSize: 18}}>Choose an image</Text>
      </TouchableOpacity>
      <ActionSheet
        containerStyle={{backgroundColor: '#0a0a0a'}}
        ref={actionSheetRef}>
        {colors && (
          <ScrollView contentContainerStyle={{padding: 40}}>
            <Text
              style={{
                fontSize: 18,
                fontWeight: '900',
                color: 'white',
                marginBottom: 20,
              }}>
              Touch any color to copy to clipboard
            </Text>
            <Color name="Dark Muted" color={colors.darkMuted} />
            <Color name="Light Muted" color={colors.lightMuted} />
            <Color name="Dark Vibrant" color={colors.darkVibrant} />
            <Color name="Dark Vibrant" color={colors.darkVibrant} />
            <Color name="Dominant Color" color={colors.dominant} />
            <TouchableOpacity
              onPress={() =>
                NativeModules.CustomModule.shareColorsToWhatsapp(
                  'Colors extracted from this image0' +
                    '\n' +
                    Object.values(colors).toString().split(',').join('\n'),
                  source.split('file://')[1],
                )
              }
              activeOpacity={0.4}
              style={{
                marginTop: 40,
                backgroundColor: '#3ddc84',
                paddingHorizontal: 20,
                paddingVertical: 16,
                borderRadius: 40,
                height: 54,
                width: '100%',
                alignItems: 'center',
              }}>
              <Text style={{fontWeight: '900', fontSize: 18}}>
                Share via whatsapp
              </Text>
            </TouchableOpacity>
          </ScrollView>
        )}
      </ActionSheet>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  sectionContainer: {
    marginTop: 32,
    paddingHorizontal: 24,
  },
  sectionTitle: {
    fontSize: 24,
    fontWeight: '600',
  },
  sectionDescription: {
    marginTop: 8,
    fontSize: 18,
    fontWeight: '400',
  },
  highlight: {
    fontWeight: '700',
  },
});

export default App;
