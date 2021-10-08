/**
 * If you are not familiar with React Navigation, refer to the "Fundamentals" guide:
 * https://reactnavigation.org/docs/getting-started
 *
 */
import { FontAwesome } from '@expo/vector-icons';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { NavigationContainer, DefaultTheme, DarkTheme } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import React, {useEffect} from 'react';
import { ColorSchemeName, DeviceEventEmitter, Linking, Pressable, ImageBackground, View, Button } from 'react-native';

import Colors from '../constants/Colors';
import useColorScheme from '../hooks/useColorScheme';
import ModalScreen from '../screens/ModalScreen';
import NotFoundScreen from '../screens/NotFoundScreen';
import TabOneScreen from '../screens/TabOneScreen';
import TabTwoScreen from '../screens/TabTwoScreen';
import { RootStackParamList, RootTabParamList, RootTabScreenProps } from '../types';
import LinkingConfiguration from './LinkingConfiguration';

export default function Navigation({ colorScheme }: { colorScheme: ColorSchemeName }) {
  return (
    <NavigationContainer
      linking={LinkingConfiguration}
      theme={colorScheme === 'dark' ? DarkTheme : DefaultTheme}>
      <RootNavigator />
    </NavigationContainer>
  );
}

/**
 * A root stack navigator is often used for displaying modals on top of all other content.
 * https://reactnavigation.org/docs/modal
 */
const Stack = createNativeStackNavigator<RootStackParamList>();

function RootNavigator() {
  return (
    <Stack.Navigator>
      <Stack.Screen name="Root" component={BottomTabNavigator} options={{ headerShown: false }} />
      <Stack.Screen name="NotFound" component={NotFoundScreen} options={{ title: 'Oops!' }} />
      <Stack.Group screenOptions={{ presentation: 'modal' }}>
        <Stack.Screen name="Modal" component={ModalScreen} />
        <Stack.Screen name="MyModal" component={ModalToneScreen} />
      </Stack.Group>
    </Stack.Navigator>
  );
}

/**
 * A bottom tab navigator displays tab buttons on the bottom of the display to switch screens.
 * https://reactnavigation.org/docs/bottom-tab-navigator
 */
const BottomTab = createBottomTabNavigator<RootTabParamList>();

function BottomTabNavigator({navigation} : any) {
  const colorScheme = useColorScheme();
  
  useEffect(() => {
    console.log("register the listener from the app")
    
    DeviceEventEmitter.addListener("ToneResponse", 
    async (event)=>{
      console.log("Event Detected")
      console.log(event)
      try {
        switch (event.actionType) {
          case 'image':{
            navigation.navigate("MyModal", {
              actionUrl: event.actionUrl
            })
            break;
          }
          case 'webpage': {
            const supported = await Linking.canOpenURL(event.actionUrl);
            console.log(supported)
            if(supported){
                await Linking.openURL(event.actionUrl)
               } else {
                alert("This url can't be open")
              }
            break;
          } 
          case 'sms': {
            try {
              const bodyMessage= "Use this cupon for 10% off in your next visit";
              console.log(event.actionUrl);
              const url = `sms:${event.actionUrl}?body=${bodyMessage}`
              await Linking.openURL(url);
            } catch (error) {
              alert("This url can't be open")
            }
            break;
          } 
          case 'tel': {
            try {
              const url = `tel:${event.actionUrl}`
              await Linking.openURL(url);
            } catch (error) {
              alert("This url can't be open")
            }
            break;
          } 
          case 'mail': {
            try {
              const bodyMessage= "Use this cupon for 10% off in your next visit";
              const subject = "CUPON CUPON CUPON"
              const url = `mailto:${event.actionUrl}?subject=${subject}&body=${bodyMessage}`
              console.log(url);
              await Linking.openURL(url);
            } catch (error) {
              alert("This url can't be open")
            }
            break;
          } 
          default:
            break;
        }
      } catch (error) {
        alert("Error handle the data");
      }
    })
    return () => {
      DeviceEventEmitter.removeListener("ToneResponse", (event)=>{
        console.log("Remove listener");
      });
    }
  }, [])

  return (
    <BottomTab.Navigator
      initialRouteName="TabOne"
      screenOptions={{
        tabBarActiveTintColor: Colors[colorScheme].tint,
      }}>
      <BottomTab.Screen
        name="TabOne"
        component={TabOneScreen}
        options={({ navigation }: RootTabScreenProps<'TabOne'>) => ({
          title: 'Tab One',
          tabBarIcon: ({ color }) => <TabBarIcon name="code" color={color} />,
          headerRight: () => (
            <Pressable
              onPress={() => navigation.navigate('Modal')}
              style={({ pressed }) => ({
                opacity: pressed ? 0.5 : 1,
              })}>
              <FontAwesome
                name="info-circle"
                size={25}
                color={Colors[colorScheme].text}
                style={{ marginRight: 15 }}
              />
            </Pressable>
          ),
        })}
      />
      <BottomTab.Screen
        name="TabTwo"
        component={TabTwoScreen}
        options={{
          title: 'Tab Two',
          tabBarIcon: ({ color }) => <TabBarIcon name="code" color={color} />,
        }}
      />
    </BottomTab.Navigator>
  );
}

/**
 * You can explore the built-in icon families and icons on the web at https://icons.expo.fyi/
 */
function TabBarIcon(props: {
  name: React.ComponentProps<typeof FontAwesome>['name'];
  color: string;
}) {
  return <FontAwesome size={30} style={{ marginBottom: -3 }} {...props} />;
}

function ModalToneScreen(props : any) {

  const { route, navigation } = props;
  const {actionUrl} = route.params;
  console.log(actionUrl)

  return (
    <View style={{ flex: 1}}>
      <ImageBackground
        resizeMode="cover"
        style = {{
          flex: 1,
          alignItems: 'center', 
          justifyContent: 'center' 
        }}
        source = {{
          uri: actionUrl
        }}
        >
        <Button onPress={() => navigation.goBack()} title="X" />
      </ImageBackground> 
    </View>
  );
}