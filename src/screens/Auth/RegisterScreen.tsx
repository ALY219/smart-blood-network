import React, { useState } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  ScrollView,
  SafeAreaView,
  KeyboardAvoidingView,
  Platform,
  Alert,
} from 'react-native';
import AppLogo from '../../components/AppLogo';

export const RegisterScreen: React.FC = () => {
  const [name, setName] = useState('');
  const [selectedRole, setSelectedRole] = useState<'Donor' | 'Patient'>('Donor');
  const [selectedBloodGroup, setSelectedBloodGroup] = useState('');
  const [nameError, setNameError] = useState(false);
  const [bloodError, setBloodError] = useState(false);

  const bloodGroups = ['A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-'];

  const handleRegister = () => {
    let isValid = true;
    if (name.trim().length < 3) {
      setNameError(true);
      isValid = false;
    } else {
      setNameError(false);
    }

    if (!selectedBloodGroup) {
      setBloodError(true);
      isValid = false;
    } else {
      setBloodError(false);
    }

    if (isValid) {
      Alert.alert(
        'Registration Success',
        `Welcome to the network, ${name}! You are registered as a ${selectedRole} (${selectedBloodGroup}).`
      );
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        style={styles.keyboardView}
      >
        <ScrollView contentContainerStyle={styles.scrollContent}>
          <View style={styles.header}>
            {/* Premium, Pure-Code Brand Logo */}
            <AppLogo isDarkTheme={true} />
            <Text style={styles.subtitle}>
              Emergency Logistics & Donor Network
            </Text>
          </View>

          {/* Registration Card Layout */}
          <View style={styles.card}>
            <Text style={styles.cardTitle}>Create Profile</Text>

            {/* Name Input Section */}
            <Text style={styles.label}>Full Name</Text>
            <View style={[styles.inputContainer, nameError && styles.inputError]}>
              <TextInput
                value={name}
                onChangeText={(text) => {
                  setName(text);
                  if (text.trim().length >= 3) {
                    setNameError(false);
                  }
                }}
                placeholder="e.g. Zainab Ahmed"
                placeholderTextColor="#8A8A8A"
                style={styles.textInput}
                autoCapitalize="words"
              />
            </View>
            {nameError && (
              <Text style={styles.errorText}>
                Please enter a valid name (min 3 characters)
              </Text>
            )}

            {/* Role Selection Section */}
            <Text style={styles.labelSection}>Select Network Role</Text>
            <View style={styles.roleContainer}>
              <TouchableOpacity
                style={[
                  styles.roleButton,
                  selectedRole === 'Donor' && styles.roleButtonActive,
                ]}
                onPress={() => setSelectedRole('Donor')}
              >
                <Text
                  style={[
                    styles.roleButtonText,
                    selectedRole === 'Donor' && styles.roleButtonTextActive,
                  ]}
                >
                  ♥ Donor
                </Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={[
                  styles.roleButton,
                  selectedRole === 'Patient' && styles.roleButtonActive,
                ]}
                onPress={() => setSelectedRole('Patient')}
              >
                <Text
                  style={[
                    styles.roleButtonText,
                    selectedRole === 'Patient' && styles.roleButtonTextActive,
                  ]}
                >
                  ✚ Patient
                </Text>
              </TouchableOpacity>
            </View>

            {/* Blood Group Selection */}
            <Text style={styles.labelSection}>Blood Group Required / Owned</Text>
            <View style={styles.bloodGrid}>
              {bloodGroups.map((bg) => {
                const isSelected = selectedBloodGroup === bg;
                return (
                  <TouchableOpacity
                    key={bg}
                    style={[
                      styles.bloodBadge,
                      isSelected && styles.bloodBadgeActive,
                    ]}
                    onPress={() => {
                      setSelectedBloodGroup(bg);
                      setBloodError(false);
                    }}
                  >
                    <Text
                      style={[
                        styles.bloodBadgeText,
                        isSelected && styles.bloodBadgeTextActive,
                      ]}
                    >
                      {bg}
                    </Text>
                  </TouchableOpacity>
                );
              })}
            </View>
            {bloodError && (
              <Text style={styles.errorText}>Please select a blood group</Text>
            )}

            {/* Register Action Button */}
            <TouchableOpacity style={styles.submitButton} onPress={handleRegister}>
              <Text style={styles.submitButtonText}>Join Logistics Network</Text>
            </TouchableOpacity>
          </View>
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#0A2540', // DeepMedicalBlue background
  },
  keyboardView: {
    flex: 1,
  },
  scrollContent: {
    paddingHorizontal: 24,
    paddingVertical: 36,
    alignItems: 'center',
  },
  header: {
    alignItems: 'center',
    marginBottom: 32,
    marginTop: 16,
  },
  subtitle: {
    color: '#E2E8F0',
    fontSize: 14,
    fontWeight: '500',
    marginTop: 8,
    textAlign: 'center',
    opacity: 0.8,
  },
  card: {
    backgroundColor: '#FFFFFF',
    borderRadius: 24,
    padding: 24,
    width: '100%',
    shadowColor: '#000000',
    shadowOffset: { width: 0, height: 10 },
    shadowOpacity: 0.15,
    shadowRadius: 15,
    elevation: 8,
  },
  cardTitle: {
    fontSize: 20,
    fontWeight: '700',
    color: '#0A2540',
    marginBottom: 20,
  },
  label: {
    fontSize: 14,
    fontWeight: '600',
    color: '#0A2540',
    marginBottom: 8,
  },
  labelSection: {
    fontSize: 14,
    fontWeight: '600',
    color: '#0A2540',
    marginTop: 20,
    marginBottom: 10,
  },
  inputContainer: {
    borderWidth: 1,
    borderColor: 'rgba(10, 37, 64, 0.15)',
    borderRadius: 12,
    backgroundColor: '#FAFAFA',
    paddingHorizontal: 16,
    height: 52,
    justifyContent: 'center',
  },
  inputError: {
    borderColor: '#DC143C',
  },
  textInput: {
    fontSize: 15,
    color: '#000000', // Explicitly enforce a crisp, highly visible black
    fontWeight: '500',
    width: '100%',
    height: '100%',
  },
  errorText: {
    color: '#DC143C',
    fontSize: 12,
    marginTop: 4,
    alignSelf: 'flex-start',
    fontWeight: '500',
  },
  roleContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  roleButton: {
    flex: 1,
    height: 52,
    borderRadius: 12,
    backgroundColor: '#F8FAFC',
    alignItems: 'center',
    justifyContent: 'center',
    flexDirection: 'row',
    marginHorizontal: 6,
    borderWidth: 1,
    borderColor: '#E2E8F0',
  },
  roleButtonActive: {
    backgroundColor: '#DC143C',
    borderColor: '#DC143C',
  },
  roleButtonText: {
    fontSize: 15,
    fontWeight: '700',
    color: '#0A2540',
  },
  roleButtonTextActive: {
    color: '#FFFFFF',
  },
  bloodGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'space-between',
  },
  bloodBadge: {
    width: '22%',
    height: 48,
    borderRadius: 10,
    backgroundColor: '#F8FAFC',
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 10,
    borderWidth: 1,
    borderColor: '#E2E8F0',
  },
  bloodBadgeActive: {
    backgroundColor: '#DC143C',
    borderColor: '#DC143C',
  },
  bloodBadgeText: {
    fontSize: 15,
    fontWeight: '700',
    color: '#0A2540',
  },
  bloodBadgeTextActive: {
    color: '#FFFFFF',
  },
  submitButton: {
    backgroundColor: '#DC143C',
    height: 52,
    borderRadius: 12,
    alignItems: 'center',
    justifyContent: 'center',
    marginTop: 24,
    shadowColor: '#DC143C',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.25,
    shadowRadius: 8,
    elevation: 4,
  },
  submitButtonText: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: '700',
  },
});

export default RegisterScreen;
