import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getAddInfoGrouped } from '../../services/addInfoService';
import {
  Box,
  Typography,
  Button,
  FormControl,
  FormLabel,
  FormGroup,
  FormControlLabel,
  Checkbox,
  Stepper,
  Step,
  StepLabel,
  Paper,
  Container,
  Grid,
  Chip,
} from '@mui/material';

const STEPS = [
  '취미 선택',
  'MBTI 유형',
  '여행 스타일',
  '동반자 유형',
  '음식 취향',
];

const INFO_TYPES = {
  1: 'hobby',
  2: 'mbti',
  3: 'travel_style',
  4: 'companion',
  5: 'food_preference',
};

const SignupQuestions = () => {
  const [activeStep, setActiveStep] = useState(0);
  const [selections, setSelections] = useState({
    hobby: [],
    mbti: [],
    travel_style: [],
    companion: [],
    food_preference: [],
  });
  const [allInfo, setAllInfo] = useState({});
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchData = async () => {
      try {
        const data = await getAddInfoGrouped();
        setAllInfo(data);
        setLoading(false);
      } catch (error) {
        console.error('Error loading questions:', error);
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  const handleNext = () => {
    if (activeStep < STEPS.length - 1) {
      setActiveStep((prevStep) => prevStep + 1);
    } else {
      handleSubmit();
    }
  };

  const handleBack = () => {
    setActiveStep((prevStep) => prevStep - 1);
  };

  const handleToggle = (value, infoType) => {
    const currentIndex = selections[infoType].indexOf(value);
    const newChecked = [...selections[infoType]];

    if (currentIndex === -1) {
      // For MBTI, only allow one selection
      if (infoType === 'mbti') {
        newChecked[0] = value;
      } else {
        newChecked.push(value);
      }
    } else {
      newChecked.splice(currentIndex, 1);
    }

    setSelections({
      ...selections,
      [infoType]: newChecked,
    });
  };

  const handleSubmit = async () => {
    try {
      // Here you would typically send the selections to your backend
      console.log('Selected preferences:', selections);
      
      // Navigate to the next page after successful submission
      navigate('/signup/complete');
    } catch (error) {
      console.error('Error submitting preferences:', error);
    }
  };

  const renderStepContent = (step) => {
    const infoType = INFO_TYPES[step + 1];
    const items = allInfo[step + 1] || [];

    if (loading) {
      return <Typography>Loading...</Typography>;
    }

    return (
      <Grid container spacing={2}>
        {items.map((item) => (
          <Grid item xs={6} sm={4} md={3} key={item.infoLowNum}>
            <Paper
              elevation={selections[infoType].includes(item.infoLowNum) ? 3 : 1}
              sx={{
                p: 2,
                cursor: 'pointer',
                backgroundColor: selections[infoType].includes(item.infoLowNum) 
                  ? 'primary.light' 
                  : 'background.paper',
                '&:hover': {
                  backgroundColor: 'action.hover',
                },
              }}
              onClick={() => handleToggle(item.infoLowNum, infoType)}
            >
              <FormControlLabel
                control={
                  <Checkbox
                    checked={selections[infoType].includes(item.infoLowNum)}
                    onChange={() => handleToggle(item.infoLowNum, infoType)}
                    color="primary"
                  />
                }
                label={item.content}
              />
            </Paper>
          </Grid>
        ))}
      </Grid>
    );
  };

  return (
    <Container maxWidth="md" sx={{ mt: 4, mb: 4 }}>
      <Paper elevation={3} sx={{ p: 4 }}>
        <Typography variant="h4" align="center" gutterBottom>
          추가 정보 입력
        </Typography>
        <Typography variant="subtitle1" align="center" color="textSecondary" paragraph>
          맞춤형 여행 정보를 제공받기 위해 추가 정보를 입력해주세요.
        </Typography>

        <Stepper activeStep={activeStep} alternativeLabel sx={{ mb: 4 }}>
          {STEPS.map((label) => (
            <Step key={label}>
              <StepLabel>{label}</StepLabel>
            </Step>
          ))}
        </Stepper>

        <Box sx={{ minHeight: '300px', mb: 4 }}>
          <Typography variant="h6" gutterBottom>
            {STEPS[activeStep]}
          </Typography>
          {renderStepContent(activeStep)}
        </Box>

        <Box sx={{ display: 'flex', justifyContent: 'space-between', pt: 2 }}>
          <Button
            onClick={handleBack}
            disabled={activeStep === 0}
            variant="outlined"
          >
            이전
          </Button>
          <Button
            variant="contained"
            color="primary"
            onClick={handleNext}
            disabled={selections[INFO_TYPES[activeStep + 1]]?.length === 0}
          >
            {activeStep === STEPS.length - 1 ? '완료' : '다음'}
          </Button>
        </Box>

        {activeStep === STEPS.length - 1 && (
          <Box sx={{ mt: 2 }}>
            <Typography variant="subtitle2" gutterBottom>
              선택한 항목:
            </Typography>
            {Object.entries(selections).map(([key, values]) => (
              values.length > 0 && (
                <Box key={key} sx={{ mb: 1 }}>
                  <Typography variant="caption" color="textSecondary">
                    {key}:
                  </Typography>
                  <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5, mt: 0.5 }}>
                    {values.map((value) => (
                      <Chip 
                        key={value} 
                        label={allInfo[Object.keys(INFO_TYPES).find(k => INFO_TYPES[k] === key)]?.find(item => item.infoLowNum === value)?.content || value}
                        size="small"
                        sx={{ mr: 0.5, mb: 0.5 }}
                      />
                    ))}
                  </Box>
                </Box>
              )
            ))}
          </Box>
        )}
      </Paper>
    </Container>
  );
};

export default SignupQuestions;
