import api from './api';

export const getAddInfoGrouped = async () => {
  try {
    const response = await api.get('/api/v1/add-info');
    return response.data;
  } catch (error) {
    console.error('Error fetching additional info:', error);
    throw error;
  }
};

export const getAddInfoByType = async (infoHighNum) => {
  try {
    const response = await api.get(`/api/v1/add-info/type/${infoHighNum}`);
    return response.data;
  } catch (error) {
    console.error(`Error fetching add info for type ${infoHighNum}:`, error);
    throw error;
  }
};

export const getAddInfoByName = async (infoName) => {
  try {
    const response = await api.get(`/api/v1/add-info/name/${infoName}`);
    return response.data;
  } catch (error) {
    console.error(`Error fetching add info for name ${infoName}:`, error);
    throw error;
  }
};
