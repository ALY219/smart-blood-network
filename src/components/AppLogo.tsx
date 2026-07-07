import React from 'react';
import { View, Text, StyleSheet } from 'react-native';

interface AppLogoProps {
  isDarkTheme?: boolean;
}

export const AppLogo: React.FC<AppLogoProps> = ({ isDarkTheme = true }) => {
  return (
    <View style={styles.container}>
      {/* Icon Emblem */}
      <View style={styles.iconContainer}>
        {/* Stylized Crimson Heart/Drop Symbol */}
        <Text style={styles.iconText}>♥</Text>
      </View>

      {/* Typography Layout */}
      <View style={styles.textContainer}>
        <Text style={[styles.smartText, { color: isDarkTheme ? '#FFFFFF' : '#0A2540' }]}>
          Smart
        </Text>
        <Text style={styles.bloodText}>
          Blood
        </Text>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
  },
  iconContainer: {
    width: 44,
    height: 44,
    borderRadius: 12,
    backgroundColor: '#DC143C',
    alignItems: 'center',
    justifyContent: 'center',
    shadowColor: '#DC143C',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 6,
    elevation: 4,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.2)',
  },
  iconText: {
    fontSize: 24,
    color: '#FFFFFF',
    fontWeight: 'bold',
  },
  textContainer: {
    flexDirection: 'row',
    marginLeft: 12,
    alignItems: 'center',
  },
  smartText: {
    fontSize: 24,
    fontWeight: '700',
  },
  bloodText: {
    fontSize: 24,
    fontWeight: '900',
    color: '#DC143C',
  },
});

export default AppLogo;
